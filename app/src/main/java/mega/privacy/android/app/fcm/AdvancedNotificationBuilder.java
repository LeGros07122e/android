package mega.privacy.android.app.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.calls.CallNotificationIntentService;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;

public final class AdvancedNotificationBuilder {

    private static final String GROUP_KEY = "Karere";
    private static final int SUMMARY_ID = 0;

    private final Context context;
    private NotificationManager notificationManager;
    private final SharedPreferences sharedPreferences;
    DatabaseHandler dbH;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    private NotificationCompat.Builder mBuilderCompat;

    public static AdvancedNotificationBuilder newInstance(Context context, MegaApiAndroid megaApi, MegaChatApiAndroid megaChatApi) {
        Context appContext = context.getApplicationContext();
        Context safeContext = ContextCompat.createDeviceProtectedStorageContext(appContext);
        if (safeContext == null) {
            safeContext = appContext;
        }
        NotificationManager notificationManager = (NotificationManager) safeContext.getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(safeContext);
        return new AdvancedNotificationBuilder(safeContext, notificationManager, sharedPreferences, megaApi, megaChatApi);
    }

    public AdvancedNotificationBuilder(Context context, NotificationManager notificationManager, SharedPreferences sharedPreferences, MegaApiAndroid megaApi, MegaChatApiAndroid megaChatApi) {
        this.context = context.getApplicationContext();
        this.notificationManager = notificationManager;
        this.sharedPreferences = sharedPreferences;
        dbH = DatabaseHandler.getDbHandler(context);
        this.megaApi = megaApi;
        this.megaChatApi = megaChatApi;
    }

//    public void updateNotification(long chatId, MegaChatMessage msg){
//        log("updateNotification");
//        MegaChatRoom chat = megaChatApi.getChatRoom(chatId);
//
//        String notificationExists = getNotificationIdByChatHandle(chat.getChatId(), msg.getMsgId());
//        if(notificationExists!=null){
//            if(notificationExists.equals("-1")){
//                log("No notif exists to update");
//            }
//            else{
//                int notificationId = (notificationExists).hashCode();
//                if(msg.isDeleted()){
//                    if(chat.getUnreadCount()!=0){
//                        MegaChatListItem item = megaChatApi.getChatListItem(chatId);
//                        if(item.getLastMessageType()!=MegaChatMessage.TYPE_INVALID){
//                            sendBundledNotification(null, null, chatId, item.getLastMessageId(), item.getLastMessage(), item.getLastMessageSender());
//                        }
//                        else{
//                            sendBundledNotification(null, null, chatId, 999, context.getString(R.string.history_cleared_message), -1);
//                        }
//                    }
//                    else{
//                        notificationManager.cancel(notificationId);
//                        removeNotification(chatId);
//                        if(!isAnyNotificationShown()){
//                            notificationManager.cancel(SUMMARY_ID);
//                        }
//                    }
//                }
//                else if(msg.isEdited()){
//                    sendBundledNotification(null, null, chatId, msg.getMsgId(), msg.getContent(), msg.getUserHandle());
//                }
//
//            }
//        }
//        else{
//            log("NULL - No notif exists to update");
//        }
//    }

//    public void removeSeenNotification(long chatId, MegaChatMessage msg){
//        log("removeSeenNotification");
//        MegaChatRoom chat = megaChatApi.getChatRoom(chatId);
//
//        String notificationExists = getNotificationIdByChatHandleAndMessageId(chat.getChatId(), msg.getMsgId());
//        if(notificationExists!=null){
//            if(notificationExists.equals("-1")){
//                log("No notif exists to update");
//            }
//            else{
//                int notificationId = (notificationExists).hashCode();
//                notificationManager.cancel(notificationId);
//                removeNotification(chatId);
//                if(!isAnyNotificationShown()){
//                    notificationManager.cancel(SUMMARY_ID);
//                }
//            }
//        }
//        else{
//            log("NULL-No notif exists to update");
//
//            notificationExists = getNotificationIdByChatHandleAndMessageId(chat.getChatId(), 999);
//            if(notificationExists!=null){
//                if(notificationExists.equals("-1")){
//                    log("No notif exists to update");
//                }
//                else{
//                    int notificationId = (notificationExists).hashCode();
//                    notificationManager.cancel(notificationId);
//                    removeNotification(chatId);
//                    if(!isAnyNotificationShown()){
//                        notificationManager.cancel(SUMMARY_ID);
//                    }
//                }
//            }
//        }
//    }

//    public void sendBundledNotification(Uri uriParameter, String vibration, long chatId, MegaChatMessage msg) {
//        log("(1) sendBundledNotification");
//        String messageContent = "";
//
//        if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
//
//            MegaNodeList nodeList = msg.getMegaNodeList();
//            if(nodeList != null) {
//                if (nodeList.size() == 1) {
//                    MegaNode node = nodeList.get(0);
//                    log("Node Name: " + node.getName());
//                    messageContent = node.getName();
//                }
//            }
//        }
//        else if(msg.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
//
//            long userCount  = msg.getUsersCount();
//
//            if(userCount==1) {
//                String name = "";
//                name = msg.getUserName(0);
//                if (name.trim().isEmpty()) {
//                    name = msg.getUserEmail(0);
//                }
//                String email = msg.getUserEmail(0);
//                log("Contact EMail: " + name);
//                messageContent = email;
//            }
//
//        }
//        else if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
//            log("Type TRUNCATE message");
//            messageContent = context.getString(R.string.history_cleared_message);
//        }
//        else{
//            messageContent = msg.getContent();
//        }
//
//        sendBundledNotification(uriParameter, vibration, chatId, msg.getMsgId(), messageContent, msg.getUserHandle());
//    }

//    public void sendBundledNotification(Uri uriParameter, String vibration, long chatId, MegaHandleList unreadMessageList) {
//        log("(1) sendBundledNotification");
//        String messageContent = "";
//
//        if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
//
//            MegaNodeList nodeList = msg.getMegaNodeList();
//            if(nodeList != null) {
//                if (nodeList.size() == 1) {
//                    MegaNode node = nodeList.get(0);
//                    log("Node Name: " + node.getName());
//                    messageContent = node.getName();
//                }
//            }
//        }
//        else if(msg.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
//
//            long userCount  = msg.getUsersCount();
//
//            if(userCount==1) {
//                String name = "";
//                name = msg.getUserName(0);
//                if (name.trim().isEmpty()) {
//                    name = msg.getUserEmail(0);
//                }
//                String email = msg.getUserEmail(0);
//                log("Contact EMail: " + name);
//                messageContent = email;
//            }
//
//        }
//        else if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
//            log("Type TRUNCATE message");
//            messageContent = context.getString(R.string.history_cleared_message);
//        }
//        else{
//            messageContent = msg.getContent();
//        }
//
//        sendBundledNotification(uriParameter, vibration, chatId, msg.getMsgId(), messageContent, msg.getUserHandle());
//    }


    public void sendBundledNotification(Uri uriParameter, String vibration, long chatId, MegaHandleList unreadHandleList) {
        log("sendBundledNotification");
        MegaChatRoom chat = megaChatApi.getChatRoom(chatId);

        ArrayList<MegaChatMessage> unreadMessages = new ArrayList<>();
        for(int i=0;i<=unreadHandleList.size();i++){
            MegaChatMessage message = megaChatApi.getMessage(chatId, unreadHandleList.get(i));
            log("Chat: "+chat.getTitle()+" messagID: "+unreadHandleList.get(i));
            if(message!=null){
                unreadMessages.add(message);
            }
            else{
                log("ERROR:message is NULL");
            }
        }

        Collections.sort(unreadMessages, new Comparator<MegaChatMessage>() {
            public int compare(MegaChatMessage c1, MegaChatMessage c2) {
                long timestamp1 = c1.getTimestamp();
                long timestamp2 = c2.getTimestamp();

                long result = timestamp2 - timestamp1;
                return (int) result;
            }
        });

        Notification notification = buildNotification(uriParameter, vibration, GROUP_KEY, chat, unreadMessages);

        int notificationId = (setNotificationId(chat.getChatId(), -1)).hashCode();
        notificationManager.notify(notificationId, notification);
        Notification summary = buildSummary(GROUP_KEY);
        notificationManager.notify(SUMMARY_ID, summary);
    }

    public void buildNotificationPreN(Uri uriParameter, String vibration, MegaChatRequest request){
        log("buildNotificationPreN");

        MegaHandleList chatHandleList = request.getMegaHandleList();

        ArrayList<MegaChatListItem> chats = new ArrayList<>();
        for(int i=0; i<chatHandleList.size(); i++){
            MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
            chats.add(chat);
        }

        PendingIntent pendingIntent = null;

        if(chatHandleList.size()>1){
            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_SUMMARY);
            intent.putExtra("CHAT_ID", -1);
            pendingIntent = PendingIntent.getActivity(context, (int)chats.get(0).getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

            //Order by last interaction
            Collections.sort(chats, new Comparator<MegaChatListItem> (){

                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                    long timestamp1 = c1.getLastTimestamp();
                    long timestamp2 = c2.getLastTimestamp();

                    long result = timestamp2 - timestamp1;
                    return (int)result;
                }
            });
        }
        else{
            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
            intent.putExtra("CHAT_ID", chats.get(0).getChatId());
            pendingIntent = PendingIntent.getActivity(context, (int)chats.get(0).getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        notificationBuilder.setColor(ContextCompat.getColor(context,R.color.mega))
                .setShowWhen(true);

        if(uriParameter!=null){
            notificationBuilder.setSound(uriParameter);
        }

        if(vibration!=null){
            if(vibration.equals("true")){
                notificationBuilder.setVibrate(new long[] {0, 500});
            }
        }

        notificationBuilder.setStyle(inboxStyle);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            //API 25 = Android 7.1
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        else{
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        notificationBuilder.setFullScreenIntent(pendingIntent, true);

        int unreadCount = 0;

        for(int i=0; i<chats.size(); i++){
            if(unreadCount<8){
                if(MegaApplication.getOpenChatId() != chats.get(i).getChatId()){
                    MegaHandleList handleListUnread = request.getMegaHandleListByChat(chats.get(i).getChatId());

                    for(int j=0;j<=handleListUnread.size();j++){
                        if(unreadCount<8){
                            log("Get message id: "+handleListUnread.get(j)+" from chatId: "+chats.get(i).getChatId());
                            MegaChatMessage message = megaChatApi.getMessage(chats.get(i).getChatId(), handleListUnread.get(j));
                            if(message!=null){

                                CharSequence cs = " ";
                                String title = chats.get(i).getTitle();
                                if(chats.get(i).isGroup()){
                                    long lastMsgSender = message.getUserHandle();

                                    MegaChatRoom chatRoom = megaChatApi.getChatRoom(chats.get(i).getChatId());
                                    String nameAction = chatRoom.getPeerFirstnameByHandle(lastMsgSender);
                                    if(nameAction==null){
                                        nameAction = "";
                                    }

                                    if(nameAction.trim().length()<=0){
                                        ChatController cC = new ChatController(context);
                                        nameAction = cC.getFirstName(lastMsgSender, chatRoom);
                                    }

                                    cs = nameAction + " @ " + title + ": " + message.getContent();
                                }
                                else{
                                    cs = title +": " + message.getContent();
                                }

                                inboxStyle.addLine(cs);
                            }
                            else{
                                log("ERROR:message is NULL");
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                }
                else{
                    log("Do not show notification - opened chat");
                }
            }
            else{
                break;
            }
        }

        String textToShow = context.getResources().getQuantityString(R.plurals.plural_number_messages_chat_notification, (int)chats.size(), chats.size());

        notificationBuilder.setContentTitle("MEGA");
        notificationBuilder.setContentText(textToShow);
        inboxStyle.setSummaryText(textToShow);

        Notification notif = notificationBuilder.build();

        if(notif!=null){
            notificationManager.notify(Constants.NOTIFICATION_PRE_N_CHAT, notif);
        }
        else{
            notificationManager.cancel(Constants.NOTIFICATION_PRE_N_CHAT);
        }
    }

    public Notification buildNotification(Uri uriParameter, String vibration, String groupKey, MegaChatRoom chat, ArrayList<MegaChatMessage> unreadMessageList) {
        log("buildNotification");
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
        intent.putExtra("CHAT_ID", chat.getChatId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)chat.getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

        String title;
        int unreadMessages = chat.getUnreadCount();
        log("Unread messages: "+unreadMessages+"  chatID: "+chat.getChatId());
        if(unreadMessages!=0){

            if(unreadMessages<0){
                unreadMessages = Math.abs(unreadMessages);
                log("unread number: "+unreadMessages);

                if(unreadMessages>1){
                    String numberString = "+"+unreadMessages;
                    title = chat.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
                else{
                    title = chat.getTitle();
                }
            }
            else{

                if(unreadMessages>1){
                    String numberString = unreadMessages+"";
                    title = chat.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
                else{
                    title = chat.getTitle();
                }
            }
        }
        else{
            title = chat.getTitle();
        }

//        Spanned notificationContent;

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setAutoCancel(true)
                .setShowWhen(true)
                .setGroup(groupKey);

        Notification.MessagingStyle messagingStyleContent = new Notification.MessagingStyle(chat.getTitle());

        int sizeFor= (int) unreadMessageList.size()-1;
        for(int i=sizeFor; i>=0;i--){
            MegaChatMessage msg = unreadMessageList.get(i);
            log("getMessage: chatID: "+chat.getChatId()+" "+unreadMessageList.get(i));
            String messageContent = "";

            if(msg!=null){
                if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                    MegaNodeList nodeList = msg.getMegaNodeList();
                    if(nodeList != null) {
                        if (nodeList.size() == 1) {
                            MegaNode node = nodeList.get(0);
                            log("Node Name: " + node.getName());
                            messageContent = node.getName();
                        }
                    }
                }
                else if(msg.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){

                    long userCount  = msg.getUsersCount();

                    if(userCount==1) {
                        String name = "";
                        name = msg.getUserName(0);
                        if (name.trim().isEmpty()) {
                            name = msg.getUserEmail(0);
                        }
                        String email = msg.getUserEmail(0);
                        log("Contact EMail: " + name);
                        messageContent = email;
                    }

                }
                else if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Type TRUNCATE message");
                    messageContent = context.getString(R.string.history_cleared_message);
                }
                else{
                    messageContent = msg.getContent();
                }

                String sender = chat.getPeerFirstnameByHandle(msg.getUserHandle());

                messagingStyleContent.addMessage(messageContent, msg.getTimestamp(), sender);
            }
            else{
                log("ERROR:buildNotification:messageNULL");
            }
        }

        messagingStyleContent.setConversationTitle(title);

        notificationBuilder.setStyle(messagingStyleContent)
                .setContentIntent(pendingIntent);

        //Set when on notification
        int size = (int) unreadMessageList.size();

        MegaChatMessage lastMsg = unreadMessageList.get(size-1);
        if(lastMsg!=null){
            log("Last message: "+lastMsg.getContent()+" "+lastMsg.getTimestamp());
            notificationBuilder.setWhen(lastMsg.getTimestamp()*1000);
        }

        if(uriParameter!=null){
            notificationBuilder.setSound(uriParameter);
        }

        if(vibration!=null){
            if(vibration.equals("true")){
                notificationBuilder.setVibrate(new long[] {0, 500});
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            //API 25 = Android 7.1
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        else{
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

//        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

//        if(chat.isGroup()){
//
//            if(msgUserHandle!=-1){
//                String nameAction = getParticipantShortName(msgUserHandle);
//
//                if(nameAction.isEmpty()){
//                    notificationBuilder.setContentText(msgContent);
//                    bigTextStyle.bigText(msgContent);
//                }
//                else{
//                    String source = "<b>"+nameAction+": </b>"+msgContent;
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        notificationContent = Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
//                    } else {
//                        notificationContent = Html.fromHtml(source);
//                    }
//                    notificationBuilder.setContentText(notificationContent);
//                    bigTextStyle.bigText(notificationContent);
//                }
//            }
//            else{
//                notificationBuilder.setContentText(msgContent);
//                bigTextStyle.bigText(msgContent);
//            }
//
//        }
//        else{
//            notificationBuilder.setContentText(msgContent);
//            bigTextStyle.bigText(msgContent);
//        }

        Bitmap largeIcon = setUserAvatar(chat);
        if(largeIcon!=null){
            log("There is avatar!");
            notificationBuilder.setLargeIcon(largeIcon);
        }

//        notificationBuilder.setStyle(bigTextStyle);

        return notificationBuilder.build();
    }

    public Bitmap setUserAvatar(MegaChatRoom chat){
        log("setUserAvatar");

        if(chat.isGroup()){
            return createDefaultAvatar(chat);
        }
        else{

            String contactMail = chat.getPeerEmail(0);

            File avatar = null;
            if (context.getExternalCacheDir() != null){
                avatar = new File(context.getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
            }
            else{
                avatar = new File(context.getCacheDir().getAbsolutePath(), contactMail + ".jpg");
            }
            Bitmap bitmap = null;
            if (avatar.exists()){
                if (avatar.length() > 0){
                    BitmapFactory.Options bOpts = new BitmapFactory.Options();
                    bOpts.inPurgeable = true;
                    bOpts.inInputShareable = true;
                    bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                    if (bitmap == null) {
                        return createDefaultAvatar(chat);
                    }
                    else{
                        return Util.getCircleBitmap(bitmap);
                    }
                }
                else{
                    return createDefaultAvatar(chat);
                }
            }
            else{
                return createDefaultAvatar(chat);
            }
        }
    }

    public Bitmap createDefaultAvatar(MegaChatRoom chat){
        log("createDefaultAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint paintText = new Paint();
        Paint paintCircle = new Paint();

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(150);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
        Typeface face = Typeface.SANS_SERIF;
        paintText.setTypeface(face);
        paintText.setAntiAlias(true);
        paintText.setSubpixelText(true);
        paintText.setStyle(Paint.Style.FILL);

        if(chat.isGroup()){
            paintCircle.setColor(ContextCompat.getColor(context,R.color.divider_upgrade_account));
        }
        else{
            String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(chat.getPeerHandle(0)));
            if(color!=null){
                log("The color to set the avatar is "+color);
                paintCircle.setColor(Color.parseColor(color));
                paintCircle.setAntiAlias(true);
            }
            else{
                log("Default color to the avatar");
                paintCircle.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
                paintCircle.setAntiAlias(true);
            }
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius,paintCircle);

        if(chat.getTitle()!=null){
            if(!chat.getTitle().isEmpty()){
                char title = chat.getTitle().charAt(0);
                String firstLetter = new String(title+"");

                if(!firstLetter.equals("(")){

                    log("Draw letter: "+firstLetter);
                    Rect bounds = new Rect();

                    paintText.getTextBounds(firstLetter,0,firstLetter.length(),bounds);
                    int xPos = (c.getWidth()/2);
                    int yPos = (int)((c.getHeight()/2)-((paintText.descent()+paintText.ascent()/2))+20);
                    c.drawText(firstLetter.toUpperCase(Locale.getDefault()), xPos, yPos, paintText);
                }

            }
        }
        return defaultAvatar;
    }

    public Notification buildSummary(String groupKey) {
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_SUMMARY);
        intent.putExtra("CHAT_ID", -1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
    }

//    public int getNotificationId() {
//        int id = sharedPreferences.getInt(NOTIFICATION_ID, SUMMARY_ID) + 1;
//        while (id == SUMMARY_ID) {
//            id++;
//        }
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(NOTIFICATION_ID, id);
//        editor.apply();
//        return id;
//    }

    public String getNotificationIdByChatHandle(long chatHandle, long messageId) {
        String chatString = MegaApiJava.userHandleToBase64(chatHandle);

        String id = sharedPreferences.getString(chatString, "-1");
        if(id!=null && (!id.equals("-1")))
            return chatString;
        else
            return null;
    }

//    public String getNotificationIdByChatHandleAndMessageId(long chatHandle, long messageId) {
//        String chatString = MegaApiJava.userHandleToBase64(chatHandle);
//        String messageString = MegaApiJava.userHandleToBase64(messageId);
//
//        String id = sharedPreferences.getString(chatString, "-1");
//        if(id.equals(messageString))
//            return chatString;
//        else
//            return null;
//
//    }

    public String setNotificationId(long chatHandle, long messageId) {
        String chatString = MegaApiJava.userHandleToBase64(chatHandle);
        String messageString = MegaApiJava.userHandleToBase64(messageId);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(chatString, messageString);
        editor.apply();
        return chatString;
    }

    public void removeNotification(long chatHandle){
        String chatString = MegaApiJava.userHandleToBase64(chatHandle);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(chatString);
        editor.apply();
    }

    public boolean isAnyNotificationShown(){
        if((sharedPreferences.getAll()).isEmpty())
            return false;
        return true;
    }

    public void removeAllChatNotifications(){
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            notificationManager.cancel(entry.getKey().hashCode());
        }
        notificationManager.cancel(SUMMARY_ID);
        sharedPreferences.edit().clear().commit();
        notificationManager.cancel(Constants.NOTIFICATION_GENERAL_PUSH_CHAT);
        notificationManager.cancel(Constants.NOTIFICATION_PRE_N_CHAT);
    }

    public void showSimpleNotification(){
        log("showSimpleNotification");

        mBuilderCompat = new NotificationCompat.Builder(context);

        if(notificationManager == null){
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_SUMMARY);
        intent.putExtra("CHAT_ID", -1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        mBuilderCompat
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).setTicker("Chat activity")
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setContentTitle("Chat activity").setContentText("You may have new messages")
                .setOngoing(false);

        notificationManager.notify(Constants.NOTIFICATION_GENERAL_PUSH_CHAT, mBuilderCompat.build());
    }

    public void showIncomingCallNotification(MegaChatCall callToAnswer, MegaChatCall callInProgress) {
        log("showIncomingCallNotification");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            MegaChatRoom chatToAnswer = megaChatApi.getChatRoom(callToAnswer.getChatid());

            MegaChatRoom chatInProgress = megaChatApi.getChatRoom(callInProgress.getChatid());
            long chatHandleInProgress = -1;
            if(chatInProgress!=null){
                chatHandleInProgress = callInProgress.getChatid();
            }
            log("showIncomingCallNotification:chatInProgress: "+callInProgress.getChatid());

//        int notificationId = Constants.NOTIFICATION_INCOMING_CALL;
            long chatCallId = callToAnswer.getId();
            String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
            int notificationId = (notificationCallId).hashCode();

            Intent ignoreIntent = new Intent(context, CallNotificationIntentService.class);
            ignoreIntent.putExtra("chatHandleInProgress", chatHandleInProgress);
            ignoreIntent.putExtra("chatHandleToAnswer", callToAnswer.getChatid());
            ignoreIntent.setAction(CallNotificationIntentService.IGNORE);
            int requestCodeIgnore = notificationId + 1;
            PendingIntent pendingIntentIgnore = PendingIntent.getService(context, requestCodeIgnore /* Request code */, ignoreIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

            Intent answerIntent = new Intent(context, CallNotificationIntentService.class);
            answerIntent.putExtra("chatHandleInProgress", chatHandleInProgress);
            answerIntent.putExtra("chatHandleToAnswer", callToAnswer.getChatid());
            answerIntent.setAction(CallNotificationIntentService.ANSWER);
            int requestCodeAnswer = notificationId + 2;
            PendingIntent pendingIntentAnswer = PendingIntent.getService(context, requestCodeAnswer /* Request code */, answerIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Action actionAnswer = new NotificationCompat.Action.Builder(-1, "ANSWER", pendingIntentAnswer).build();
            NotificationCompat.Action actionIgnore = new NotificationCompat.Action.Builder(-1, "IGNORE", pendingIntentIgnore).build();

            long[] pattern = {0, 1000, 1000, 1000, 1000, 1000, 1000};

            //No sound just vibration
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_notify_download)
                    .setContentTitle(chatToAnswer.getPeerFullname(0))
                    .setContentText(context.getString(R.string.notification_subtitle_incoming))
                    .setAutoCancel(false)
                    .setVibrate(pattern)
                    .setColor(ContextCompat.getColor(context,R.color.mega))
                    .addAction(actionIgnore)
                    .addAction(actionAnswer);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
                //API 25 = Android 7.1
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            }
            else{
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }

            notificationBuilder.setFullScreenIntent(pendingIntentAnswer, true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Bitmap largeIcon = setUserAvatar(chatToAnswer);
                if(largeIcon!=null){
                    notificationBuilder.setLargeIcon(largeIcon);
                }
            }

            if(notificationManager == null){
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        else{
            log("Not supported incoming call notification: "+Build.VERSION.SDK_INT);
        }
    }

    public void checkQueuedCalls(){
        log("checkQueuedCalls");

        MegaHandleList handleList = megaChatApi.getChatCalls();
        if(handleList!=null){
            long numberOfCalls = handleList.size();
            log("checkQueuedCalls: Number of calls in progress: "+numberOfCalls);
            if (numberOfCalls>1){
                log("checkQueuedCalls: MORE than one call in progress: "+numberOfCalls);
                MegaChatCall callInProgress = null;
                MegaChatCall callIncoming = null;

                for(int i=0; i<handleList.size(); i++){
                    MegaChatCall call = megaChatApi.getChatCall(handleList.get(i));
                    if(call!=null){
                        log("Call ChatID: "+call.getChatid()+" Status: "+call.getStatus());
                        if((call.getStatus()>=MegaChatCall.CALL_STATUS_IN_PROGRESS) && (call.getStatus()<MegaChatCall.CALL_STATUS_TERMINATING)){
                            callInProgress = call;
                            log("FOUND Call in progress: "+callInProgress.getChatid());
                            break;
                        }
                    }
                }

                if(callInProgress==null){
                    long openCallChatId = MegaApplication.getOpenCallChatId();
                    log("openCallId: "+openCallChatId);
                    if(openCallChatId!=-1){
                        MegaChatCall possibleCall = megaChatApi.getChatCall(openCallChatId);
                        if(possibleCall.getStatus()<MegaChatCall.CALL_STATUS_TERMINATING){
                            callInProgress = possibleCall;
                            log("FOUND Call activity shown: "+callInProgress.getChatid());
                        }
                    }
                }

                for(int i=0; i<handleList.size(); i++){
                    MegaChatCall call = megaChatApi.getChatCall(handleList.get(i));
                    if(call!=null){

                        if(call.getStatus()<MegaChatCall.CALL_STATUS_IN_PROGRESS && (!call.isIgnored())){

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if(notificationManager == null){
                                    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                }

                                StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
                                boolean shown=false;

                                long chatCallId = call.getId();
                                String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
                                int notificationId = (notificationCallId).hashCode();

                                log("Active Notifications: "+ notifs.length);
                                for(int k = 0; k< notifs.length; k++){
                                    if(notifs[k].getId()==notificationId){
                                        log("Notification for this call already shown");
                                        shown = true;
                                        break;
                                    }
                                }

                                if(!shown){
                                    if(callInProgress.getId()!=call.getId()){
                                        callIncoming = call;
                                        log("(1) FOUND Call incoming and NOT shown and NOT ignored: "+callIncoming.getChatid());
                                        break;
                                    }
                                }
                            }
                            else{
                                callIncoming = call;
                                log("(2) FOUND Call incoming and NOT shown and NOT ignored: "+callIncoming.getChatid());
                                break;
                            }
                        }
                    }
                }

                if(callInProgress!=null){
                    if(callIncoming!=null){
                        showIncomingCallNotification(callIncoming, callInProgress);
                    }
                    else{
                        log("ERROR:callIncoming is NULL");
                    }
                }
                else{
                    log("callInProgress NOT found");
                }
            }
            else{
                log("checkQueuedCalls: No calls to launch");
            }
        }
    }

    public void showMissedCallNotification(MegaChatCall call) {
        log("showMissedCallNotification");

        MegaChatRoom chat = megaChatApi.getChatRoom(call.getChatid());

        String notificationContent = chat.getPeerFullname(0);

        long chatCallId = call.getId();
        String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
        int notificationId = (notificationCallId).hashCode() + Constants.NOTIFICATION_MISSED_CALL;

        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
        intent.putExtra("CHAT_ID", chat.getChatId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)chat.getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

        long[] pattern = {0, 1000};

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setContentTitle(context.getString(R.string.missed_call_notification_title))
                .setContentText(notificationContent)
                .setAutoCancel(true)
                .setVibrate(pattern)
                .setSound(defaultSoundUri)
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            //API 25 = Android 7.1
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        else{
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        notificationBuilder.setFullScreenIntent(pendingIntent, true);

        if(chat.getPeerEmail(0)!=null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Bitmap largeIcon = setUserAvatar(chat);
                if(largeIcon!=null){
                    notificationBuilder.setLargeIcon(largeIcon);
                }
            }
        }

        if(notificationManager == null){
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void generateChatNotification(MegaChatRequest request){
        log("generateChatNotification");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            String manufacturer = "xiaomi";
            if(!manufacturer.equalsIgnoreCase(Build.MANUFACTURER)) {
                log("generateChatNotification:POST Android N");
                newGenerateChatNotification(request);
            }
            else{
                log("generateChatNotification:XIAOMI POST Android N");
                generateChatNotificationPreN(request);
            }
        }
        else {
            log("generateChatNotification:PRE Android N");
            generateChatNotificationPreN(request);
        }
    }

    public void newGenerateChatNotification(MegaChatRequest request){
        log("newGenerateChatNotification");
        boolean beep = request.getFlag();
        log("should beep: "+beep);

        MegaHandleList chatHandleList = request.getMegaHandleList();
        ArrayList<MegaChatListItem> chats = new ArrayList<>();
        for(int i=0; i<chatHandleList.size(); i++){
            MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
            chats.add(chat);
        }

        //Order by last interaction
        Collections.sort(chats, new Comparator<MegaChatListItem> (){

            public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                long timestamp1 = c1.getLastTimestamp();
                long timestamp2 = c2.getLastTimestamp();

                long result = timestamp2 - timestamp1;
                return (int)result;
            }
        });

        //Check if the last chat notification is enabled
        log("generateChatNotification for: "+chats.size()+" chats");
        long lastChatId = -1;
        if(chats!=null && (!(chats.isEmpty()))){
            lastChatId = chats.get(0).getChatId();
        }

        boolean showNotif = false;

        if(MegaApplication.getOpenChatId() != lastChatId){

            MegaHandleList handleListUnread = request.getMegaHandleListByChat(lastChatId);

            showNotif = showChatNotification(lastChatId, handleListUnread, beep);
            if(!showNotif){
                log("Muted chat - do not show notification");
            }

            if(beep){
                beep=false;
            }
        }
        else{
            log("Do not show notification - opened chat");
        }

        log("generateChatNotification for: "+chats.size()+" chats");
        if(showNotif){
            for(int i=1; i<chats.size(); i++){
                if(MegaApplication.getOpenChatId() != chats.get(i).getChatId()){

                    MegaHandleList handleListUnread = request.getMegaHandleListByChat(chats.get(i).getChatId());

                    showChatNotification(chats.get(i).getChatId(), handleListUnread, beep);
                    if(beep){
                        beep=false;
                    }
                }
                else{
                    log("Do not show notification - opened chat");
                }
            }
        }
        else{
            log("Mute for the last chat");
        }
    }

    public void generateChatNotificationPreN(MegaChatRequest request){
        log("generateChatNotificationPreN");
        boolean beep = request.getFlag();
        log("should beep: "+beep);

        MegaHandleList chatHandleList = request.getMegaHandleList();
        ArrayList<MegaChatListItem> chats = new ArrayList<>();
        for(int i=0; i<chatHandleList.size(); i++){
            MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
            chats.add(chat);
        }

        //Order by last interaction
        Collections.sort(chats, new Comparator<MegaChatListItem> (){

            public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                long timestamp1 = c1.getLastTimestamp();
                long timestamp2 = c2.getLastTimestamp();

                long result = timestamp2 - timestamp1;
                return (int)result;
            }
        });

        log("generateChatNotification for: "+chats.size()+" chats");
        long lastChatId = -1;
        if(chats!=null && (!(chats.isEmpty()))){
            lastChatId = chats.get(0).getChatId();
        }

        showChatNotificationPreN(request, beep, lastChatId);
    }

    public void showChatNotificationPreN(MegaChatRequest request, boolean beep, long lastChatId){
        log("showChatNotification");

        if(beep){
            ChatSettings chatSettings = dbH.getChatSettings();

            if (chatSettings != null) {
                if (chatSettings.getNotificationsEnabled().equals("true")) {
                    log("Notifications ON for all chats");

                    ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(lastChatId));

                    if (chatItemPreferences == null) {
                        log("No preferences for this item");

                        if (chatSettings.getNotificationsSound() == null){
                            log("Notification sound is NULL");
                            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                            buildNotificationPreN(defaultSoundUri, chatSettings.getVibrationEnabled(), request);

                        }
                        else if(chatSettings.getNotificationsSound().equals("-1")){
                            log("Silent notification Notification sound -1");
                            buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                        }
                        else{
                            String soundString = chatSettings.getNotificationsSound();
                            Uri uri = Uri.parse(soundString);
                            log("Uri: " + uri);

                            if (soundString.equals("true") || soundString.equals("")) {

                                Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                                buildNotificationPreN(defaultSoundUri, chatSettings.getVibrationEnabled(), request);
                            } else if (soundString.equals("-1")) {
                                log("Silent notification");
                                buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                            } else {
                                Ringtone sound = RingtoneManager.getRingtone(context, uri);
                                if (sound == null) {
                                    log("Sound is null");
                                    buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                                } else {
                                    buildNotificationPreN(uri, chatSettings.getVibrationEnabled(), request);
                                }
                            }
                        }

                    } else {
                        log("Preferences FOUND for this item");
                        if (chatItemPreferences.getNotificationsEnabled().equals("true")) {
                            log("Notifications ON for this chat");
                            String soundString = chatItemPreferences.getNotificationsSound();

                            if (soundString.equals("true")||soundString.isEmpty()) {
                                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                buildNotificationPreN(defaultSoundUri2, chatSettings.getVibrationEnabled(), request);
                            } else if (soundString.equals("-1")) {
                                log("Silent notification");
                                buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                            } else {
                                Uri uri = Uri.parse(soundString);
                                log("Uri: " + uri);
                                Ringtone sound = RingtoneManager.getRingtone(context, uri);
                                if (sound == null) {
                                    log("Sound is null");
                                    buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                                } else {
                                    buildNotificationPreN(uri, chatSettings.getVibrationEnabled(), request);

                                }
                            }
                        } else {
                            log("Notifications OFF for this chats");
                        }
                    }
                } else {
                    log("Notifications OFF");
                }
            } else {
                log("Notifications DEFAULT ON");

                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                buildNotificationPreN(defaultSoundUri2, "true", request);
            }
        }
        else{
            buildNotificationPreN(null, "false", request);
        }
    }

    public boolean showChatNotification(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("showChatNotification: "+beep);

        if(beep){

            ChatSettings chatSettings = dbH.getChatSettings();
            if (chatSettings != null) {
                if (chatSettings.getNotificationsEnabled()==null){
                    log("getNotificationsEnabled NULL --> Notifications ON");

                    return checkNotificationsSound(chatid, handleListUnread, beep);
                }
                else{
                    if (chatSettings.getNotificationsEnabled().equals("true")) {
                        log("Notifications ON for all chats");

                        return checkNotificationsSound(chatid, handleListUnread, beep);
                    } else {
                        log("Notifications OFF");
                        return false;
                    }
                }

            } else {
                log("Notifications DEFAULT ON");

                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                sendBundledNotification(defaultSoundUri2, "true", chatid, handleListUnread);
                return true;
            }
        }
        else{
            sendBundledNotification(null, "false", chatid, handleListUnread);
            return true;
        }
    }

    public boolean checkNotificationsSound(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("checkNotificationsSound: "+beep);

        ChatSettings chatSettings = dbH.getChatSettings();
        ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(chatid));

        if (chatItemPreferences == null) {
            log("No preferences for this item");

            if (chatSettings.getNotificationsSound() == null){
                log("Notification sound is NULL");
                Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                sendBundledNotification(defaultSoundUri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);

            }
            else if(chatSettings.getNotificationsSound().equals("-1")){
                log("Silent notification Notification sound -1");
                sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
            }
            else{
                String soundString = chatSettings.getNotificationsSound();
                Uri uri = Uri.parse(soundString);
                log("Uri: " + uri);

                if (soundString.equals("true") || soundString.equals("")) {

                    Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                    sendBundledNotification(defaultSoundUri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else if (soundString.equals("-1")) {
                    log("Silent notification");
                    sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else {
                    Ringtone sound = RingtoneManager.getRingtone(context, uri);
                    if (sound == null) {
                        log("Sound is null");
                        sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                    } else {
                        sendBundledNotification(uri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                    }
                }
            }
            return true;
        } else {
            log("Preferences FOUND for this item");
            if (chatItemPreferences.getNotificationsEnabled().equals("true")) {
                log("Notifications ON for this chat");
                String soundString = chatItemPreferences.getNotificationsSound();

                if (soundString.equals("true")||soundString.isEmpty()) {
                    Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    sendBundledNotification(defaultSoundUri2, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else if (soundString.equals("-1")) {
                    log("Silent notification");
                    sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else {
                    Uri uri = Uri.parse(soundString);
                    log("Uri: " + uri);
                    Ringtone sound = RingtoneManager.getRingtone(context, uri);
                    if (sound == null) {
                        log("Sound is null");
                        sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                    } else {
                        sendBundledNotification(uri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);

                    }
                }
                return true;
            } else {
                log("Notifications OFF for this chat");
                return false;
            }
        }
    }

    public static void log(String message) {
        Util.log("AdvancedNotificationBuilder", message);
    }

}
