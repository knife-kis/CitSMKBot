package io.cit.CitSMKBot.service;

import io.cit.CitSMKBot.config.BotConfig;
import io.cit.CitSMKBot.model.User;
import io.cit.CitSMKBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String ZAV = "ZAVED";
    private static final String INJ = "INJINER";
    static final String HELP_TEXT = "Данный бот служит в качестве вспомогательного материала к РК ИЛ ЦИТ \n\n" +
            "Команада /start Позволяет начать использование бота";
    @Autowired
    private UserRepository userRepository;

    final BotConfig config;



    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList();
        listOfCommands.add(new BotCommand("/start", "Начало работы"));
//        listOfCommands.add(new BotCommand("/myData", "Текущая дата"));
        listOfCommands.add(new BotCommand("/help", "Помощь в командах"));
//        listOfCommands.add(new BotCommand("/settings", "Настройки"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default: sendMessage(chatId,"Команда не поддерживается");
            }
        }

        else if (update.hasCallbackQuery()){
            long messageID = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (update.getCallbackQuery().getData().equals(ZAV)) {
                String text = "Какой раздел вы хотите уточнить?";
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int) messageID);
                tryExecuteMessageEdit(message);
            }
            else if (update.getCallbackQuery().getData().equals(INJ)){
                String text = "Какой раздел вы хотите уточнить?";
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int) messageID);
                tryExecuteMessageEdit(message);
            }
        }
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

    private void startCommandReceived(long chatId, String name){

        SendMessage message;
        message = sendMessage(chatId, answerStart(name));
        sendMessageButton(createButtons(), message);
        tryExecuteMessage(message);

    }

    private void sendMessageButton(InlineKeyboardMarkup markup, SendMessage message) {
        message.setReplyMarkup(markup);
    }

    private InlineKeyboardMarkup createButtons() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(createRoleButton("Заведующий", ZAV));
        rowInLine.add(createRoleButton("Инженер", INJ));

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

    private String answerStart(String name) {
        String answer = "Добро пожаловать, " + name + ", темперь мы победим СМК \n\n" +
                "Какая ваша должность?";
        log.info("Зашел пользователь " + name);
        return answer;
    }

    private SendMessage sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }

    private void tryExecuteMessage(SendMessage message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void tryExecuteMessageEdit(EditMessageText message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
