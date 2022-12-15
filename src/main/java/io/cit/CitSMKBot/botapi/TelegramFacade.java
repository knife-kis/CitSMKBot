package io.cit.CitSMKBot.botapi;

import io.cit.CitSMKBot.cache.UserDataCache;
import io.cit.CitSMKBot.model.User;
import io.cit.CitSMKBot.model.UserRepository;
import io.cit.CitSMKBot.service.MainMenuService;
import io.cit.CitSMKBot.service.ReplyMessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Обработка сообщений
 */
@Component
@Slf4j
public class TelegramFacade {
    private BotStateContext botStateContext;
    private UserDataCache userDataCache;
    private ReplyMessagesService messagesService;

    @Autowired
    private UserRepository userRepository;

    public TelegramFacade(BotStateContext botStateContext,
                          UserDataCache userDataCache,
                          ReplyMessagesService messagesService) {
        this.botStateContext = botStateContext;
        this.userDataCache = userDataCache;
        this.messagesService = messagesService;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        SendMessage replyMessage = null;

        if (update.hasCallbackQuery()) {    // Проверка на кнопки
            CallbackQuery callbackQuery = update.getCallbackQuery();
            log.info("New callbackQuery from User: {}, userId: {}, with data: {}", update.getCallbackQuery().getFrom().getUserName(),
                    callbackQuery.getFrom().getId(), update.getCallbackQuery().getData());
            return processCallbackQuery(callbackQuery);
        }

        Message message = update.getMessage();  // Проверка на текстовое сообщение
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("New message from User:{}, chatId: {},  with text: {}",
                    message.getFrom().getUserName(), message.getChatId(), message.getText());
            replyMessage = handleInputMessage(message);
        }

        return replyMessage;
    }

    private SendMessage handleInputMessage(Message message) {
        String inputMsg = message.getText();
        Long userId = message.getFrom().getId();
        BotState botState;
        SendMessage replyMessage;

        switch (inputMsg) {
            case "/start":
                registerUser(message);
                botState = BotState.WHO_ARE_YOU;
                break;
            case "Помощь":
                    botState = BotState.SHOW_HELP_MENU;
                break;
            default:
                botState = userDataCache.getUsersCurrentBotState(userId);
                break;
        }

        userDataCache.setUsersCurrentBotState(userId, botState);

        replyMessage = botStateContext.processInputMessage(botState, message);

        return replyMessage;
    }
    private void registerUser(Message msg) {

        if(userRepository.findById(msg.getChatId()).isEmpty()){

            long chatId = msg.getChatId();
            Chat chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("Пользователь сохранен: " + user);
        }
    }

    private BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();
        SendMessage callBackAnswer = null;

        //From Destiny choose buttons
        if (buttonQuery.getData().equals("CHIEF")) {
            callBackAnswer = messagesService.getReplyMessage(chatId, "bot.askChiefSection");
            userDataCache.setUsersCurrentBotState(userId, BotState.WHICH_SECTION);
            callBackAnswer.setReplyMarkup(getInlineMessageButtonsSectionChief());
        }
        //From Gender choose buttons
        else if (buttonQuery.getData().equals("ENG")) {
            userDataCache.setUsersCurrentBotState(userId, BotState.WHICH_SECTION);
            callBackAnswer = messagesService.getReplyMessage(chatId, "bot.askEngineerSection");
        } else {
            userDataCache.setUsersCurrentBotState(userId, BotState.SHOW_MAIN_MENU);
        }
        return callBackAnswer;
    }

    private ReplyKeyboard getInlineMessageButtonsSectionChief() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineRow1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineRow2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineRow3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineRow4 = new ArrayList<>();

        rowInLineRow1.add(createRoleButton("Персонал", "P"));
        rowInLineRow1.add(createRoleButton("Помещение", "O"));
        rowInLineRow1.add(createRoleButton("Оборудование", "OB"));
        rowInLineRow1.add(createRoleButton("услуги", "YS"));

        rowInLineRow2.add(createRoleButton("Договора", "DOG"));
        rowInLineRow2.add(createRoleButton("Методов", "METO"));
        rowInLineRow2.add(createRoleButton("Отбор", "OTBOR"));
        rowInLineRow2.add(createRoleButton("Несоответствия", "NES"));

        rowInLineRow3.add(createRoleButton("Записи", "NES"));
        rowInLineRow3.add(createRoleButton("Неопределенность", "NES"));
        rowInLineRow3.add(createRoleButton("Риски", "NES"));
        rowInLineRow3.add(createRoleButton("Корректир", "NES"));

        rowInLineRow4.add(createRoleButton("Аудит", "NES"));

        rowsInLine.add(rowInLineRow1);
        rowsInLine.add(rowInLineRow2);
        rowsInLine.add(rowInLineRow3);
        rowsInLine.add(rowInLineRow4);
        markup.setKeyboard(rowsInLine);

        return markup;
    }

    private InlineKeyboardButton createRoleButton(String post, String postData) {
        var button = new InlineKeyboardButton();
        button.setText(post);
        button.setCallbackData(postData);
        return button;

    }
}
