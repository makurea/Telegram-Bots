# main.py
import config
from aiogram import Bot, Dispatcher, types
import aiohttp
import urllib.parse

# Инициализация бота и диспетчера
bot = Bot(token=config.TOKEN)
dp = Dispatcher(bot)

# Приветственное сообщение
welcome_message = """
Привет! Я бот, который поможет тебе найти название аниме по отправленному скриншоту или по ссылке на изображение. Просто отправь мне фотографию или ссылку на изображение, и я постараюсь найти название аниме на ней.
"""


# Обработчик команды /start
@dp.message_handler(commands=['start'])
async def send_welcome(message: types.Message):
    await message.reply(welcome_message)


# Обработчик для сообщений с фотографиями
@dp.message_handler(content_types=types.ContentType.PHOTO)
async def handle_photo(message: types.Message):
    # Получаем информацию о файле фотографии
    photo = message.photo[-1]
    file_id = photo.file_id

    # Получаем путь к файлу фотографии
    file_path = await bot.get_file(file_id)
    url = file_path.file_path

    # Отправляем GET запрос к API для поиска аниме по изображению
    anime_info = await search_anime_by_image(url)

    # Отправляем результат в чат
    await send_anime_info(message, anime_info)


# Обработчик для сообщений с текстом (ссылкой на изображение)
@dp.message_handler(content_types=types.ContentType.TEXT)
async def handle_text(message: types.Message):
    # Проверяем, является ли текст ссылкой на изображение
    if message.text.startswith('http'):
        # Отправляем GET запрос к API для поиска аниме по изображению
        anime_info = await search_anime_by_image(message.text)

        # Отправляем результат в чат
        await send_anime_info(message, anime_info)
    else:
        # Ответ на текстовое сообщение, если оно не содержит ссылку на изображение
        await message.reply("Пожалуйста, отправьте мне фотографию или ссылку на изображение.")


# Функция для поиска аниме по изображению с использованием API
async def search_anime_by_image(image_url):
    url = "https://api.trace.moe/search?anilistInfo&url={}".format(urllib.parse.quote_plus(image_url))
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            if response.status == 200:
                data = await response.json()
                return data.get('result')[0] if data.get('result') else None
    return None


# Функция для отправки информации об аниме в чат
async def send_anime_info(message, anime_info):
    if anime_info:
        anime_title = anime_info.get('filename', 'Unknown Title')
        similarity = anime_info.get('similarity', 0)
        episode = anime_info.get('episode', 'Unknown Episode')

        # Используем английское название, если оно доступно, иначе используем японское
        anime_title_english = anime_info['anilist']['title']['english'] if anime_info['anilist']['title'][
            'english'] else anime_info['anilist']['title']['native']

        response_text = f"Название аниме: {anime_title_english}\n" \
                        f"Схожесть: {similarity:.2f}\n" \
                        f"Эпизод: {episode}"
    else:
        response_text = "Кажется, я не смог найти аниме на этом изображении 😔"

    await message.reply(response_text)


# Запуск бота
if __name__ == '__main__':
    import asyncio

    loop = asyncio.get_event_loop()
    loop.run_until_complete(dp.skip_updates())
    loop.run_until_complete(dp.start_polling())
