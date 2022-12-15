package io.cit.CitSMKBot.cache;


import io.cit.CitSMKBot.botapi.BotState;
import io.cit.CitSMKBot.botapi.handlers.fillingprofile.UserProfileData;

public interface DataCache {
    void setUsersCurrentBotState(long userId, BotState botState);

    BotState getUsersCurrentBotState(long userId);

    UserProfileData getUserProfileData(long userId);

    void saveUserProfileData(long userId, UserProfileData userProfileData);
}
