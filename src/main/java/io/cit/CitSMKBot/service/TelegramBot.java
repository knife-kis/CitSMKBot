package io.cit.CitSMKBot.service;

import io.cit.CitSMKBot.botapi.TelegramFacade;
import io.cit.CitSMKBot.config.BotConfig;
import io.cit.CitSMKBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private TelegramFacade telegramFacade;

    public TelegramBot(TelegramFacade telegramFacade, BotConfig config){
        this.telegramFacade = telegramFacade;
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList();
        listOfCommands.add(new BotCommand("/start", "Начало работы"));
        listOfCommands.add(new BotCommand("/help", "Помощь в командах"));
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
        BotApiMethod<?> replyMessageToUser = telegramFacade.handleUpdate(update);
        tryExecuteMessage(replyMessageToUser);
    }

    private void tryExecuteMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
     }
}
