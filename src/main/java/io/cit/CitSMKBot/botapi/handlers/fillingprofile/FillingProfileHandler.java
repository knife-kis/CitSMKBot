package io.cit.CitSMKBot.botapi.handlers.fillingprofile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import io.cit.CitSMKBot.botapi.BotState;
import io.cit.CitSMKBot.botapi.InputMessageHandler;
import io.cit.CitSMKBot.cache.UserDataCache;
import io.cit.CitSMKBot.service.ReplyMessagesService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


/**
 * Формирует анкету пользователя.
 */

@Slf4j
@Component
public class FillingProfileHandler implements InputMessageHandler {
    private UserDataCache userDataCache;
    private ReplyMessagesService messagesService;

    public FillingProfileHandler(UserDataCache userDataCache,
                                 ReplyMessagesService messagesService) {
        this.userDataCache = userDataCache;
        this.messagesService = messagesService;
    }

    @Override
    public SendMessage handle(Message message) {
        return processUsersInput(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.WHICH_SECTION;
    }

    private SendMessage processUsersInput(Message inputMsg) {
        long userId = inputMsg.getFrom().getId();
        long chatId = inputMsg.getChatId();

        UserProfileData profileData = userDataCache.getUserProfileData(userId);
        BotState botState = userDataCache.getUsersCurrentBotState(userId);

        SendMessage replyToUser = null;

        if (botState.equals(BotState.WHO_ARE_YOU)) {
            replyToUser = messagesService.getReplyMessage(chatId, "bot.whoAreYou");
            userDataCache.setUsersCurrentBotState(userId, BotState.WHICH_SECTION);
            replyToUser.setReplyMarkup(getInlineMessageButtons());
        }

        if (botState.equals(BotState.WHICH_SECTION)) {
            replyToUser = messagesService.getReplyMessage(chatId, "bot.whichSection");
            userDataCache.setUsersCurrentBotState(userId, BotState.WHO_ARE_YOU);
            replyToUser.setReplyMarkup(getInlineMessageButtons());
        }

        userDataCache.saveUserProfileData(userId, profileData);

        return replyToUser;
    }
    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(createRoleButton("Заведующий", "CHIEF"));
        rowInLine.add(createRoleButton("Инженер", "ENG"));

        rowsInLine.add(rowInLine);
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



