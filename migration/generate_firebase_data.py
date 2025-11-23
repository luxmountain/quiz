import json

# Sample data templates
LEVEL_TEMPLATES = [
    {
        "name": "Beginner",
        "nameVi": "Cơ bản",
        "description": "Foundation vocabulary for daily life",
        "descriptionVi": "Từ vựng nền tảng cho cuộc sống hàng ngày",
        "imageUrl": "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800"
    },
    {
        "name": "Intermediate",
        "nameVi": "Trung cấp",
        "description": "Expand your vocabulary knowledge",
        "descriptionVi": "Mở rộng kiến thức từ vựng của bạn",
        "imageUrl": "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800"
    }
]

TOPIC_TEMPLATES = [
    ("Daily Routine", "Hoạt động hàng ngày", "https://images.unsplash.com/photo-1495364141860-b0d03eccd065?w=800"),
    ("Food & Drinks", "Đồ ăn & Đồ uống", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"),
    ("Travel & Transportation", "Du lịch & Phương tiện", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"),
    ("Family & Friends", "Gia đình & Bạn bè", "https://images.unsplash.com/photo-1511895426328-dc8714191300?w=800"),
    ("Work & Study", "Làm việc & Học tập", "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800"),
    ("Health & Fitness", "Sức khỏe & Thể dục", "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=800"),
    ("Shopping", "Mua sắm", "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=800"),
    ("Weather & Nature", "Thời tiết & Thiên nhiên", "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800"),
    ("Technology", "Công nghệ", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800"),
    ("Entertainment", "Giải trí", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800")
]

FLASHCARD_TEMPLATES = [
    ("breakfast", "/ˈbrek.fəst/", "bữa sáng", "noun", "danh từ", "I usually have <b><u>breakfast</u></b> at 7 AM.", "Tôi thường ăn sáng lúc 7 giờ.", "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=800"),
    ("exercise", "/ˈek.sə.saɪz/", "tập thể dục", "verb", "động từ", "She likes to <b><u>exercise</u></b> every day.", "Cô ấy thích tập thể dục mỗi ngày.", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800"),
    ("tired", "/taɪərd/", "mệt mỏi", "adjective", "tính từ", "I feel very <b><u>tired</u></b> today.", "Hôm nay tôi cảm thấy rất mệt.", "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=800"),
    ("happy", "/ˈhæp.i/", "vui vẻ", "adjective", "tính từ", "She looks very <b><u>happy</u></b> today.", "Cô ấy trông rất vui hôm nay.", "https://images.unsplash.com/photo-1554080353-a576cf803bda?w=800"),
    ("study", "/ˈstʌd.i/", "học tập", "verb", "động từ", "I <b><u>study</u></b> English every day.", "Tôi học tiếng Anh mỗi ngày.", "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800"),
    ("beautiful", "/ˈbjuː.tɪ.fəl/", "đẹp", "adjective", "tính từ", "What a <b><u>beautiful</u></b> day!", "Thật là một ngày đẹp trời!", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=800"),
    ("quickly", "/ˈkwɪk.li/", "nhanh chóng", "adverb", "trạng từ", "Please come here <b><u>quickly</u></b>.", "Hãy đến đây nhanh lên.", "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800"),
    ("important", "/ɪmˈpɔː.tənt/", "quan trọng", "adjective", "tính từ", "This is very <b><u>important</u></b>.", "Điều này rất quan trọng.", "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800"),
    ("understand", "/ˌʌn.dəˈstænd/", "hiểu", "verb", "động từ", "Do you <b><u>understand</u></b> me?", "Bạn có hiểu tôi không?", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"),
    ("friendly", "/ˈfrend.li/", "thân thiện", "adjective", "tính từ", "He is very <b><u>friendly</u></b>.", "Anh ấy rất thân thiện.", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=800")
]

CONVERSATION_TEMPLATES = [
    {
        "title": "Morning Routine",
        "titleVi": "Thói quen buổi sáng",
        "imageUrl": "https://images.pexels.com/photos/296301/pexels-photo-296301.jpeg?w=800",
        "context": "Talking about daily morning activities",
        "contextVi": "Nói về các hoạt động buổi sáng hàng ngày"
    },
    {
        "title": "At the Restaurant",
        "titleVi": "Tại nhà hàng",
        "imageUrl": "https://images.pexels.com/photos/3201921/pexels-photo-3201921.jpeg?w=800",
        "context": "Ordering food at a restaurant",
        "contextVi": "Gọi món tại nhà hàng"
    },
    {
        "title": "Shopping",
        "titleVi": "Mua sắm",
        "imageUrl": "https://images.pexels.com/photos/974964/pexels-photo-974964.jpeg?w=800",
        "context": "Shopping for clothes",
        "contextVi": "Mua quần áo"
    },
    {
        "title": "At the Airport",
        "titleVi": "Tại sân bay",
        "imageUrl": "https://images.pexels.com/photos/2007401/pexels-photo-2007401.jpeg?w=800",
        "context": "Check-in at the airport",
        "contextVi": "Làm thủ tục tại sân bay"
    },
    {
        "title": "Making Friends",
        "titleVi": "Kết bạn",
        "imageUrl": "https://images.pexels.com/photos/1496371/pexels-photo-1496371.jpeg?w=800",
        "context": "Meeting new people",
        "contextVi": "Gặp gỡ người mới"
    },
    {
        "title": "At the Gym",
        "titleVi": "Tại phòng gym",
        "imageUrl": "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?w=800",
        "context": "Exercising at the gym",
        "contextVi": "Tập thể dục tại phòng gym"
    },
    {
        "title": "Doctor's Visit",
        "titleVi": "Đi khám bác sĩ",
        "imageUrl": "https://images.pexels.com/photos/4173251/pexels-photo-4173251.jpeg?w=800",
        "context": "Visiting the doctor",
        "contextVi": "Đi khám bệnh"
    },
    {
        "title": "Job Interview",
        "titleVi": "Phỏng vấn xin việc",
        "imageUrl": "https://images.pexels.com/photos/5668882/pexels-photo-5668882.jpeg?w=800",
        "context": "Interview for a job",
        "contextVi": "Phỏng vấn xin việc"
    },
    {
        "title": "Weekend Plans",
        "titleVi": "Kế hoạch cuối tuần",
        "imageUrl": "https://images.pexels.com/photos/2168974/pexels-photo-2168974.jpeg?w=800",
        "context": "Discussing weekend activities",
        "contextVi": "Thảo luận hoạt động cuối tuần"
    },
    {
        "title": "Asking for Directions",
        "titleVi": "Hỏi đường",
        "imageUrl": "https://images.pexels.com/photos/2467506/pexels-photo-2467506.jpeg?w=800",
        "context": "Finding the way to a place",
        "contextVi": "Tìm đường đến một địa điểm"
    }
]

def generate_flashcard(base_index, topic_index, card_index):
    template = FLASHCARD_TEMPLATES[card_index % len(FLASHCARD_TEMPLATES)]
    flashcard_id = f"flashcard_{base_index:03d}"
    
    return {
        "id": flashcard_id,
        "word": template[0],
        "pronunciation": template[1],
        "meaning": template[2],
        "wordType": template[3],
        "wordTypeVi": template[4],
        "imageUrl": template[7],
        "contextSentence": template[5],
        "contextSentenceVi": template[6],
        "example": f"This is example sentence {card_index + 1} for {template[0]}.",
        "exampleVi": f"Đây là câu ví dụ {card_index + 1} cho từ {template[2]}.",
        "order": card_index + 1,
        "difficulty": "easy" if card_index < 5 else "medium"
    }

def generate_topic(level_index, topic_index, flashcard_start_index):
    topic_id = f"topic_{(level_index * 10 + topic_index + 1):03d}"
    template_index = topic_index % len(TOPIC_TEMPLATES)
    template = TOPIC_TEMPLATES[template_index]
    
    flashcards = []
    for i in range(10):
        flashcard = generate_flashcard(flashcard_start_index + i, topic_index, i)
        flashcards.append(flashcard)
    
    return {
        "id": topic_id,
        "name": f"{template[0]} {topic_index + 1}",
        "nameVi": f"{template[1]} {topic_index + 1}",
        "description": f"Learn vocabulary about {template[0].lower()}",
        "descriptionVi": f"Học từ vựng về {template[1].lower()}",
        "imageUrl": template[2],
        "order": topic_index + 1,
        "totalWords": 10,
        "createdAt": 1699488000000,
        "updatedAt": 1699488000000,
        "flashcards": flashcards
    }

def generate_conversation(conv_index):
    template = CONVERSATION_TEMPLATES[conv_index % len(CONVERSATION_TEMPLATES)]
    conv_id = f"conversation_{(conv_index + 1):03d}"
    
    dialogue = [
        {
            "speaker": "Person A",
            "text": f"Hello! Let's talk about {template['title'].lower()}.",
            "textVi": f"Xin chào! Hãy nói về {template['titleVi'].lower()}.",
            "order": 0,
            "vocabularyWord": "hello",
            "question": "What does 'hello' mean?",
            "questionVi": "'hello' có nghĩa là gì?",
            "options": [
                {"id": "a", "text": "xin chào", "isCorrect": True},
                {"id": "b", "text": "tạm biệt", "isCorrect": False}
            ]
        },
        {
            "speaker": "Person B",
            "text": "Sure! I'd love to discuss this topic.",
            "textVi": "Chắc chắn rồi! Tôi rất muốn thảo luận chủ đề này.",
            "order": 1,
            "vocabularyWord": "discuss",
            "question": "What does 'discuss' mean?",
            "questionVi": "'discuss' có nghĩa là gì?",
            "options": [
                {"id": "a", "text": "thảo luận", "isCorrect": True},
                {"id": "b", "text": "từ chối", "isCorrect": False}
            ]
        },
        {
            "speaker": "Person A",
            "text": "What do you usually do in this situation?",
            "textVi": "Bạn thường làm gì trong tình huống này?",
            "order": 2
        }
    ]
    
    vocabulary = [
        {
            "word": "hello",
            "meaning": "xin chào",
            "pronunciation": "/həˈloʊ/",
            "wordType": "interjection",
            "wordTypeVi": "thán từ"
        },
        {
            "word": "discuss",
            "meaning": "thảo luận",
            "pronunciation": "/dɪˈskʌs/",
            "wordType": "verb",
            "wordTypeVi": "động từ"
        }
    ]
    
    return {
        "id": conv_id,
        "title": f"{template['title']} {conv_index + 1}",
        "titleVi": f"{template['titleVi']} {conv_index + 1}",
        "imageUrl": template['imageUrl'],
        "contextDescription": template['context'],
        "contextDescriptionVi": template['contextVi'],
        "order": conv_index + 1,
        "createdAt": 1699488000000,
        "dialogue": dialogue,
        "vocabularyWords": vocabulary
    }

def generate_firebase_data():
    data = {
        "levels": {},
        "conversations": {},
        "settings": {
            "app": {
                "version": "1.0.0",
                "minSupportedVersion": "1.0.0",
                "maintenanceMode": False,
                "dailyGoal": 10,
                "reminderEnabled": True,
                "reminderTime": "20:00",
                "soundEnabled": True,
                "autoPlayAudio": True
            }
        }
    }
    
    flashcard_counter = 1
    
    # Generate 2 levels
    for level_index in range(2):
        level_id = f"level_{(level_index + 1):03d}"
        template = LEVEL_TEMPLATES[level_index]
        
        topics = {}
        for topic_index in range(10):
            topic = generate_topic(level_index, topic_index, flashcard_counter)
            topics[topic["id"]] = topic
            flashcard_counter += 10
        
        data["levels"][level_id] = {
            "id": level_id,
            "name": template["name"],
            "nameVi": template["nameVi"],
            "description": template["description"],
            "descriptionVi": template["descriptionVi"],
            "order": level_index + 1,
            "totalTopics": 10,
            "imageUrl": template["imageUrl"],
            "topics": topics
        }
    
    # Generate 10 conversations
    for conv_index in range(10):
        conversation = generate_conversation(conv_index)
        data["conversations"][conversation["id"]] = conversation
    
    return data

# Generate and save the data
if __name__ == "__main__":
    firebase_data = generate_firebase_data()
    
    with open('firebase-data-v2.json', 'w', encoding='utf-8') as f:
        json.dump(firebase_data, f, ensure_ascii=False, indent=4)
    
    print("✅ Firebase data generated successfully!")
    print(f"📊 Total levels: 2")
    print(f"📚 Total topics: 20 (10 per level)")
    print(f"📝 Total flashcards: 200 (10 per topic)")
    print(f"💬 Total conversations: 10")
