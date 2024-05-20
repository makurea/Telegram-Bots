# подключаем модуль для Телеграма
import telebot
import requests

# указываем токен для доступа к боту
bot = telebot.TeleBot('YOUR_TOKEN')

# приветственный текст
start_txt = 'Привет! Это бот прогноза погоды. \n\nОтправьте боту название города и он скажет, какая там температура и как она ощущается.'

# Общая функция для получения описания погоды на основе идентификатора погоды (weather_id)
def get_weather_description(weather_id):
    if weather_id == 800:
        return "Ясно ☀️"
    elif 801 <= weather_id <= 804:
        return "Облачно ☁️"
    elif 701 <= weather_id <= 781:
        return "Туманно 🌫️"
    elif 600 <= weather_id <= 622:
        return "Снег ❄️"
    elif 500 <= weather_id <= 531:
        return "Дождь 🌧️"
    elif 300 <= weather_id <= 321:
        return "Легкий дождь 🌦️"
    elif 200 <= weather_id <= 232:
        return "Гроза ⛈️"
    else:
        return "Неизвестно 🤔"

# Функция для получения иконки погоды на основе идентификатора погоды (weather_id)
def get_weather_icon(weather_id):
    if weather_id == 800:
        return "01d"  # ясно
    elif 801 <= weather_id <= 804:
        return "02d"  # облачно
    elif 600 <= weather_id <= 622:
        return "13d"  # снег
    elif 500 <= weather_id <= 531:
        return "10d"  # дождь
    elif 300 <= weather_id <= 321:
        return "09d"  # легкий дождь
    elif 200 <= weather_id <= 232:
        return "11d"  # гроза
    else:
        return "01d"  # по умолчанию, ясно

# обрабатываем старт бота
@bot.message_handler(commands=['start'])
def start(message):
    bot.send_message(message.chat.id, start_txt)

# обрабатываем любой текстовый запрос
@bot.message_handler(content_types=['text'])
def weather(message):
    city = message.text
    # формируем запрос
    url = f'https://api.openweathermap.org/data/2.5/weather?q={city}&units=metric&lang=ru&appid=YOUR_API_KEY'
    # отправляем запрос на сервер и сразу получаем результат
    weather_data = requests.get(url).json()
    
    # если запрос успешен
    if 'main' in weather_data:
        # получаем данные о погоде
        temperature = round(weather_data['main']['temp'])
        feels_like = round(weather_data['main']['feels_like'])
        description = get_weather_description(weather_data['weather'][0]['id'])
        wind_speed = round(weather_data['wind']['speed'])
        humidity = weather_data['main']['humidity']
        pressure = weather_data['main']['pressure']
        min_temp = round(weather_data['main']['temp_min'])
        max_temp = round(weather_data['main']['temp_max'])
        sunrise = weather_data['sys']['sunrise']
        sunset = weather_data['sys']['sunset']
        icon_code = get_weather_icon(weather_data['weather'][0]['id'])

        # формируем ответ
        response = f"Сейчас в городе {city} {temperature}°C.\n" \
                   f"Ощущается как {feels_like}°C.\n" \
                   f"{description}\n" \
                   f"Скорость ветра: {wind_speed} м/c.\n" \
                   f"Влажность: {humidity}%\n" \
                   f"Атмосферное давление: {pressure} гПа.\n" \
                   f"Минимальная температура: {min_temp}°C.\n" \
                   f"Максимальная температура: {max_temp}°C."

        # отправляем ответ пользователю
        bot.send_message(message.chat.id, response)

    else:
        # если запрос неудачен
        bot.send_message(message.chat.id, "Извините, не удалось получить данные о погоде для данного города.")

# запускаем бота
if __name__ == '__main__':
    bot.polling(none_stop=True)
