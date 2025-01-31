//WeatherBot.java
package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.Duration;
import java.time.LocalTime;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(WeatherBot.class);

    // Планировщик для рассылки
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Конструктор
    public WeatherBot() {
        super(); // Вызов конструктора родительского класса
        startScheduler(); // Запуск планировщика
    }

    // Запуск планировщика
    private void startScheduler() {
        logger.info("Запуск планировщика рассылки...");

        // Утренняя рассылка (8:00)
        scheduler.scheduleAtFixedRate(() -> sendNotifications(1), getDelayUntil(8, 0), 24 * 60, TimeUnit.MINUTES);

        // Дневная рассылка (12:00)
        scheduler.scheduleAtFixedRate(() -> sendNotifications(2), getDelayUntil(12, 0), 24 * 60, TimeUnit.MINUTES);

        // Вечерняя рассылка (18:00)
        scheduler.scheduleAtFixedRate(() -> sendNotifications(3), getDelayUntil(18, 0), 24 * 60, TimeUnit.MINUTES);
    }

    // Вычисление задержки до указанного времени
    private long getDelayUntil(int hour, int minute) {
        LocalTime now = LocalTime.now(); // Текущее время
        LocalTime targetTime = LocalTime.of(hour, minute); // Время, до которого нужно вычислить задержку

        long delay;
        if (now.isBefore(targetTime)) {
            // Если текущее время раньше целевого, задержка = разница между целевым и текущим временем
            delay = Duration.between(now, targetTime).toMinutes();
        } else {
            // Если текущее время позже целевого, задержка = разница до целевого времени следующего дня
            delay = Duration.between(now, targetTime.plusHours(24)).toMinutes();
        }

        logger.info("Задержка до {}:{}: {} минут", hour, minute, delay);
        return delay;
    }

    // Отправка уведомлений
    private void sendNotifications(int notificationTime) {
        logger.info("Начало рассылки для времени: {}", notificationTime);

        for (Map.Entry<Long, UserData> entry : userDataMap.entrySet()) {
            long chatId = entry.getKey();
            UserData userData = entry.getValue();

            if (userData.getNotificationTime() == notificationTime || userData.getNotificationTime() == 4) {
                logger.info("Отправка уведомления пользователю (Chat ID: {}).", chatId);
                handleWeatherRequest(chatId);
            }
        }
    }

    // Остановка планировщика при завершении работы бота
    @Override
    public void onClosing() {
        scheduler.shutdown();
        logger.info("Планировщик рассылки остановлен.");
        super.onClosing();
    }

    // Хранение данных пользователей
    private final Map<Long, UserData> userDataMap = new HashMap<>();

    // Константы для кнопок
    private static final String WEATHER_BUTTON = "🌤️ Погода";
    private static final String SETTINGS_BUTTON = "⚙️ Настройки";
    private static final String CHANGE_CITY_BUTTON = "🏙️ Изменить город";
    private static final String CHANGE_NOTIFICATION_BUTTON = "⏰ Изменить рассылку";
    private static final String[] NOTIFICATION_OPTIONS = {"🚫 Нет рассылки", "🌅 Утром", "🌞 Днём", "🌆 Вечером", "🌞🌆 Весь день"};


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();

            logger.info("Получено сообщение от пользователя (Chat ID: {}): {}", chatId, text);

            if (text.equals("/start")) {
                logger.info("Пользователь (Chat ID: {}) начал взаимодействие с ботом.", chatId);
                sendWelcomeMessage(chatId);
            } else if (text.equals(WEATHER_BUTTON)) {
                logger.info("Пользователь (Chat ID: {}) запросил погоду.", chatId);
                handleWeatherRequest(chatId);
            } else if (text.equals(SETTINGS_BUTTON)) {
                logger.info("Пользователь (Chat ID: {}) открыл настройки.", chatId);
                sendSettingsMenu(chatId);
            } else if (text.equals(CHANGE_CITY_BUTTON)) {
                logger.info("Пользователь (Chat ID: {}) выбрал изменение города.", chatId);
                sendMessage(chatId, "Введите название вашего города:");
            } else if (text.equals(CHANGE_NOTIFICATION_BUTTON)) {
                logger.info("Пользователь (Chat ID: {}) выбрал изменение времени рассылки.", chatId);
                sendNotificationOptions(chatId);
            } else if (isNotificationOption(text)) {
                logger.info("Пользователь (Chat ID: {}) выбрал время рассылки: {}", chatId, text);
                handleNotificationSelection(chatId, text);
            } else {
                logger.info("Пользователь (Chat ID: {}) ввёл город: {}", chatId, text);
                handleCityInput(chatId, text);
            }
        }
    }

    // Отправка приветственного сообщения
    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "👋 Привет! Я бот, который поможет тебе узнать погоду.\n\n" +
                "Для начала, скажи, из какого ты города?";
        logger.info("Отправка приветственного сообщения пользователю (Chat ID: {}).", chatId);
        sendMessage(chatId, welcomeText);
    }

    // Обработка ввода города
    private void handleCityInput(long chatId, String city) {
        UserData userData = userDataMap.getOrDefault(chatId, new UserData(chatId, city, 0));
        userData.setCity(city);
        userDataMap.put(chatId, userData);

        logger.info("Город пользователя (Chat ID: {}) сохранён: {}", chatId, city);

        String response = "✅ Город сохранён: " + city + "\n\n" +
                "Теперь выбери, в какое время ты хочешь получать уведомления о погоде:";
        sendNotificationOptions(chatId);
    }

    // Отправка опций для выбора времени рассылки
    private void sendNotificationOptions(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String option : NOTIFICATION_OPTIONS) {
            KeyboardRow row = new KeyboardRow();
            row.add(option);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);

        logger.info("Отправка опций рассылки пользователю (Chat ID: {}).", chatId);
        sendMessageWithKeyboard(chatId, "Выбери время рассылки:", keyboardMarkup);
    }

    // Обработка выбора времени рассылки
    private void handleNotificationSelection(long chatId, String selectedOption) {
        UserData userData = userDataMap.get(chatId);
        if (userData != null) {
            int notificationTime = getNotificationTimeFromOption(selectedOption);
            userData.setNotificationTime(notificationTime);
            userDataMap.put(chatId, userData);

            logger.info("Время рассылки пользователя (Chat ID: {}) изменено на: {}", chatId, selectedOption);

            String response = "✅ Время рассылки сохранено: " + selectedOption + "\n\n" +
                    "Теперь ты можешь узнать погоду или изменить настройки.";
            sendMainMenu(chatId, response);
        }
    }

    // Получение времени рассылки из выбранной опции
    private int getNotificationTimeFromOption(String option) {
        switch (option) {
            case "🌅 Утром":
                return 1;
            case "🌞 Днём":
                return 2;
            case "🌆 Вечером":
                return 3;
            case "🌞🌆 Весь день":
                return 4;
            default:
                return 0;
        }
    }

    // Отправка главного меню
    private void sendMainMenu(long chatId, String text) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(WEATHER_BUTTON);
        row.add(SETTINGS_BUTTON);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);

        logger.info("Отправка главного меню пользователю (Chat ID: {}).", chatId);
        sendMessageWithKeyboard(chatId, text, keyboardMarkup);
    }

    // Отправка меню настроек
    private void sendSettingsMenu(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(CHANGE_CITY_BUTTON);
        row.add(CHANGE_NOTIFICATION_BUTTON);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);

        logger.info("Отправка меню настроек пользователю (Chat ID: {}).", chatId);
        sendMessageWithKeyboard(chatId, "⚙️ Настройки:", keyboardMarkup);
    }

    // Обработка запроса погоды
    private void handleWeatherRequest(long chatId) {
        UserData userData = userDataMap.get(chatId);
        if (userData != null) {
            String city = userData.getCity();
            logger.info("Запрос погоды для города: {} (Chat ID: {}).", city, chatId);

            try {
                WeatherData weatherData = WeatherApiClient.fetchWeatherData(city, Config.OPENWEATHERMAP_API_KEY);
                if (weatherData != null) {
                    String response = formatWeatherResponse(city, weatherData);
                    logger.info("Данные о погоде для города {} успешно получены (Chat ID: {}).", city, chatId);
                    sendMessage(chatId, response);
                } else {
                    logger.warn("Не удалось получить данные о погоде для города: {} (Chat ID: {}).", city, chatId);
                    sendMessage(chatId, "❌ Не удалось получить данные о погоде для города: " + city);
                }
            } catch (Exception e) {
                logger.error("Ошибка при запросе погоды для города: {} (Chat ID: {}).", city, chatId, e);
                sendMessage(chatId, "❌ Произошла ошибка при запросе данных о погоде.");
            }
        } else {
            logger.warn("Пользователь (Chat ID: {}) не указал город.", chatId);
            sendMessage(chatId, "❌ Сначала введите ваш город.");
        }
    }

    // Форматирование ответа с данными о погоде
    private String formatWeatherResponse(String city, WeatherData weatherData) {
        return String.format("🌤️ Погода в городе %s:\n" +
                        "🌡️ Температура: %d°C\n" +
                        "🌡️ Ощущается как: %d°C\n" +
                        "🌬️ Ветер: %d м/c\n" +
                        "💧 Влажность: %d%%\n" +
                        "🌡️ Давление: %d гПа\n" +
                        "🌅 Минимальная температура: %d°C\n" +
                        "🌞 Максимальная температура: %d°C",
                city, weatherData.getTemperature(), weatherData.getFeelsLike(),
                weatherData.getWindSpeed(), weatherData.getHumidity(), weatherData.getPressure(),
                weatherData.getMinTemp(), weatherData.getMaxTemp());
    }

    // Проверка, является ли текст опцией рассылки
    private boolean isNotificationOption(String text) {
        for (String option : NOTIFICATION_OPTIONS) {
            if (option.equals(text)) {
                return true;
            }
        }
        return false;
    }

    // Отправка сообщения с клавиатурой
    private void sendMessageWithKeyboard(long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
            logger.info("Сообщение с клавиатурой отправлено пользователю (Chat ID: {}).", chatId);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения с клавиатурой (Chat ID: {}).", chatId, e);
        }
    }

    // Отправка простого сообщения
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
            logger.info("Сообщение отправлено пользователю (Chat ID: {}): {}", chatId, text);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения (Chat ID: {}).", chatId, e);
        }
    }

    @Override
    public String getBotUsername() {
        return "WeatherBot";
    }

    @Override
    public String getBotToken() {
        return Config.BOT_TOKEN;
    }

    // Точка входа для запуска бота
    public static void main(String[] args) {
        try {
            logger.info("Запуск бота...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new WeatherBot());
            logger.info("Бот успешно запущен.");
        } catch (TelegramApiException e) {
            logger.error("Ошибка при запуске бота.", e);
        }
    }
}