package com.dailyenglish.checkin

import java.util.*

data class WordItem(
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String,
    val exampleCn: String
)

object Words {

    val all = listOf(
        WordItem("serendipity", "/ˌserənˈdɪpəti/", "意外发现珍奇事物的幸运",
            "I found this lovely café by pure serendipity.", "我完全是偶然发现了这家可爱的咖啡馆。"),
        WordItem("resilient", "/rɪˈzɪliənt/", "有韧性的；能迅速恢复的",
            "Children are often more resilient than adults.", "孩子往往比成年人更有韧性。"),
        WordItem("elaborate", "/ɪˈlæbərət/", "详尽的；精心制作的",
            "Could you elaborate on that point?", "你能详细说明一下那一点吗？"),
        WordItem("subtle", "/ˈsʌtl/", "微妙的；不明显的",
            "There is a subtle difference between the two words.", "这两个词之间有细微的差别。"),
        WordItem("momentum", "/məˈmentəm/", "势头；动力",
            "Let's keep the momentum going.", "让我们保持这股劲头。"),
        WordItem("cherish", "/ˈtʃerɪʃ/", "珍惜；珍爱",
            "Cherish every moment with your family.", "珍惜与家人在一起的每一刻。"),
        WordItem("procrastinate", "/prəˈkræstɪneɪt/", "拖延；耽搁",
            "Stop procrastinating and start writing.", "别再拖延了，动笔吧。"),
        WordItem("versatile", "/ˈvɜːrsətl/", "多才多艺的；多用途的",
            "English is a versatile tool for communication.", "英语是一门用途广泛的交流工具。"),
        WordItem("genuine", "/ˈdʒenjuɪn/", "真诚的；真正的",
            "She has a genuine interest in art.", "她对艺术有真挚的兴趣。"),
        WordItem("alleviate", "/əˈliːvieɪt/", "减轻；缓解",
            "A short walk can alleviate stress.", "散散步可以缓解压力。"),
        WordItem("thrive", "/θraɪv/", "茁壮成长；蓬勃发展",
            "Some people thrive under pressure.", "有些人在压力下表现出色。"),
        WordItem("meticulous", "/məˈtɪkjələs/", "一丝不苟的",
            "She is meticulous about spelling.", "她对拼写一丝不苟。"),
        WordItem("abundant", "/əˈbʌndənt/", "丰富的；充裕的",
            "The region has abundant rainfall.", "该地区雨量充沛。"),
        WordItem("eloquent", "/ˈeləkwənt/", "雄辩的；有说服力的",
            "He gave an eloquent speech at the ceremony.", "他在典礼上发表了有说服力的演讲。"),
        WordItem("embrace", "/ɪmˈbreɪs/", "拥抱；欣然接受",
            "Embrace change and keep learning.", "拥抱变化，持续学习。"),
        WordItem("endure", "/ɪnˈdjʊr/", "忍耐；持久",
            "True friendship endures over time.", "真正的友谊经得起时间考验。"),
        WordItem("vivid", "/ˈvɪvɪd/", "生动的；鲜明的",
            "She gave a vivid description of the trip.", "她生动地描述了那次旅行。"),
        WordItem("humble", "/ˈhʌmbl/", "谦虚的；谦逊的",
            "He remains humble despite his success.", "尽管取得了成功，他依然谦虚。"),
        WordItem("inevitable", "/ɪnˈevɪtəbl/", "不可避免的",
            "Mistakes are inevitable when you practice.", "练习时犯错是难免的。"),
        WordItem("concise", "/kənˈsaɪs/", "简洁的；简明的",
            "Keep your answer clear and concise.", "回答要清晰简洁。"),
        WordItem("curiosity", "/ˌkjʊəriˈɒsəti/", "好奇心；求知欲",
            "Curiosity is the engine of learning.", "好奇心是学习的引擎。"),
        WordItem("persistent", "/pəˈsɪstənt/", "坚持不懈的",
            "Be persistent and you will improve.", "坚持下去，你就会进步。"),
        WordItem("delicate", "/ˈdelɪkət/", "精致的；细腻的",
            "Pronunciation is a delicate skill.", "发音是一门细腻的技巧。"),
        WordItem("worthwhile", "/ˌwɜːθˈwaɪl/", "值得的；有意义的",
            "Reading every day is worthwhile.", "每天阅读都是值得的。"),
        WordItem("fluent", "/ˈfluːənt/", "流利的；流畅的",
            "She speaks fluent English after years of practice.", "多年练习后，她的英语说得很流利。"),
        WordItem("inspire", "/ɪnˈspaɪər/", "激励；启发",
            "Teachers inspire us to think for ourselves.", "老师启发我们独立思考。"),
        WordItem("accumulate", "/əˈkjuːmjəleɪt/", "积累；积聚",
            "Vocabulary accumulates with daily review.", "词汇靠日常复习一点点积累。"),
        WordItem("endeavor", "/ɪnˈdevə/", "努力；尽力",
            "Learning a language is a long endeavor.", "学一门语言是场漫长的旅程。"),
        WordItem("precise", "/prɪˈsaɪs/", "精确的；准确的",
            "Try to use precise words when you write.", "写作时尽量用词准确。"),
        WordItem("tranquil", "/ˈtræŋkwɪl/", "宁静的；安宁的",
            "The garden is tranquil in the early morning.", "清晨的花园十分宁静。")
    )

    fun indexForToday(): Int {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.YEAR) * 366L + c.get(Calendar.DAY_OF_YEAR)
        return (day % all.size).toInt()
    }
}
