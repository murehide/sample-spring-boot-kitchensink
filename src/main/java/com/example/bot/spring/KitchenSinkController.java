/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.security.SecureRandom;  
import javax.crypto.Cipher;  
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MemberJoinedEvent;
import com.linecorp.bot.model.event.MemberLeftEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ContentProvider;
import com.linecorp.bot.model.event.message.FileMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.ImagemapExternalLink;
import com.linecorp.bot.model.message.imagemap.ImagemapVideo;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.sender.Sender;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class KitchenSinkController {
    private static final String MISSION_MESSAGE = "楽しむを世界へ";
    private static final String PHILOSOPHY_MESSAGE = "信頼され愛される企業をめざし、\r\nたゆまぬ努力をしつづけます。\r\n"
            + "一. 仕事を楽しむ\r\n"
            + "一. 成長を楽しむ\r\n"
            + "一. 挑戦を楽しむ\r\n"
            + "一. サービスを楽しむ\r\n"
            + "一. 感謝を楽しむ\r\n";
    private static final String CORE_VALUE_01_MESSAGE = "1.常に前向きに行動しよう";
    private static final String CORE_VALUE_02_MESSAGE = "2.何事にも一生懸命、一所懸命やろう";
    private static final String CORE_VALUE_03_MESSAGE = "3.日本一のマナーを実践しよう";
    private static final String CORE_VALUE_04_MESSAGE = "4.NO.1をつくろう";
    private static final String CORE_VALUE_05_MESSAGE = "5.スピードを追求し喜びを与えよう";
    private static final String CORE_VALUE_06_MESSAGE = "6.全ての人にワクワクを仕掛けよう";
    private static final String CORE_VALUE_07_MESSAGE = "7.自分力を磨こう";
    private static final String CORE_VALUE_08_MESSAGE = "8.素直で謙虚になろう";
    private static final String CORE_VALUE_09_MESSAGE = "9.家族のようなチームをつくろう";
    private static final String CORE_VALUE_10_MESSAGE = "10.夢・希望を強く思い続け現実にしよう";
    private static final String CORE_VALUE_ALL_MESSAGE = CORE_VALUE_01_MESSAGE + "\r\n"
            + CORE_VALUE_02_MESSAGE + "\r\n"
            + CORE_VALUE_03_MESSAGE + "\r\n"
            + CORE_VALUE_04_MESSAGE + "\r\n"
            + CORE_VALUE_05_MESSAGE + "\r\n"
            + CORE_VALUE_06_MESSAGE + "\r\n"
            + CORE_VALUE_07_MESSAGE + "\r\n"
            + CORE_VALUE_08_MESSAGE + "\r\n"
            + CORE_VALUE_09_MESSAGE + "\r\n"
            + CORE_VALUE_10_MESSAGE + "\r\n";

    private static final String OTHER_MESSAGE = "您還需要其他協助嗎?";
    
    private static final Map<String, String> MESSAGE_MAP = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;

        {
           put("mission", MISSION_MESSAGE);
            put("MISSION", MISSION_MESSAGE);
            put("ミッション", MISSION_MESSAGE);
            put("みっしょん", MISSION_MESSAGE);

            put("philosophy", PHILOSOPHY_MESSAGE);
            put("PHILOSOPHY", PHILOSOPHY_MESSAGE);
            put("けいえいりねん", PHILOSOPHY_MESSAGE);
            put("経営理念", PHILOSOPHY_MESSAGE);

            // コアバリュー1
            put("corevalue1", CORE_VALUE_01_MESSAGE);
            put("COREVALUE1", CORE_VALUE_01_MESSAGE);
            put("こあばりゅー1", CORE_VALUE_01_MESSAGE);
            put("コアバリュー1", CORE_VALUE_01_MESSAGE);

            // コアバリュー2
            put("corevalue2", CORE_VALUE_02_MESSAGE);
            put("COREVALUE2", CORE_VALUE_02_MESSAGE);
            put("こあばりゅー2", CORE_VALUE_02_MESSAGE);
            put("コアバリュー2", CORE_VALUE_02_MESSAGE);

            // コアバリュー3
            put("corevalue3", CORE_VALUE_03_MESSAGE);
            put("COREVALUE3", CORE_VALUE_03_MESSAGE);
            put("こあばりゅー3", CORE_VALUE_03_MESSAGE);
            put("コアバリュー3", CORE_VALUE_03_MESSAGE);

            // コアバリュー4
            put("corevalue4", CORE_VALUE_04_MESSAGE);
            put("COREVALUE4", CORE_VALUE_04_MESSAGE);
            put("こあばりゅー4", CORE_VALUE_04_MESSAGE);
            put("コアバリュー4", CORE_VALUE_04_MESSAGE);          

            // コアバリュー5
            put("corevalue5", CORE_VALUE_05_MESSAGE);
            put("COREVALUE5", CORE_VALUE_05_MESSAGE);
            put("こあばりゅー5", CORE_VALUE_05_MESSAGE);
            put("コアバリュー5", CORE_VALUE_05_MESSAGE);

            // コアバリュー6
            put("corevalue6", CORE_VALUE_06_MESSAGE);
            put("COREVALUE6", CORE_VALUE_06_MESSAGE);
            put("こあばりゅー6", CORE_VALUE_06_MESSAGE);
            put("コアバリュー6", CORE_VALUE_06_MESSAGE);

            // コアバリュー7
            put("corevalue7", CORE_VALUE_07_MESSAGE);
            put("COREVALUE7", CORE_VALUE_07_MESSAGE);
            put("こあばりゅー7", CORE_VALUE_07_MESSAGE);
            put("コアバリュー7", CORE_VALUE_07_MESSAGE);

            // コアバリュー8
            put("corevalue8", CORE_VALUE_08_MESSAGE);
            put("COREVALUE8", CORE_VALUE_08_MESSAGE);
            put("こあばりゅー8", CORE_VALUE_08_MESSAGE);
            put("コアバリュー8", CORE_VALUE_08_MESSAGE);

            // コアバリュー9
            put("corevalue9", CORE_VALUE_09_MESSAGE);
            put("COREVALUE9", CORE_VALUE_09_MESSAGE);
            put("こあばりゅー9", CORE_VALUE_09_MESSAGE);
            put("コアバリュー9", CORE_VALUE_09_MESSAGE);

            // コアバリュー10
            put("corevalue10", CORE_VALUE_10_MESSAGE);
            put("COREVALUE10", CORE_VALUE_10_MESSAGE);
            put("こあばりゅー10", CORE_VALUE_10_MESSAGE);
            put("コアバリュー10", CORE_VALUE_10_MESSAGE);

            // コアバリューすべて
            put("corevalue", CORE_VALUE_ALL_MESSAGE);
            put("COREVALUE", CORE_VALUE_ALL_MESSAGE);
            put("こあばりゅー", CORE_VALUE_ALL_MESSAGE);
            put("コアバリュー", CORE_VALUE_ALL_MESSAGE);

        }
    });
    
    private byte[] byteRe;
    
    private String encrytStr="";

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private LineBlobClient lineBlobClient;

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        handleSticker(event.getReplyToken(), event.getMessage());
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        LocationMessageContent locationMessage = event.getMessage();
        reply(event.getReplyToken(), new LocationMessage(
                locationMessage.getTitle(),
                locationMessage.getAddress(),
                locationMessage.getLatitude(),
                locationMessage.getLongitude()
        ));
    }

    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        // You need to install ImageMagick
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    final ContentProvider provider = event.getMessage().getContentProvider();
                    final DownloadedContent jpg;
                    final DownloadedContent previewImg;
                    if (provider.isExternal()) {
                        jpg = new DownloadedContent(null, provider.getOriginalContentUrl());
                        previewImg = new DownloadedContent(null, provider.getPreviewImageUrl());
                    } else {
                        jpg = saveContent("jpg", responseBody);
                        previewImg = createTempFile("jpg");
                        system(
                                "convert",
                                "-resize", "240x",
                                jpg.path.toString(),
                                previewImg.path.toString());
                    }
                    reply(event.getReplyToken(),
                          new ImageMessage(jpg.getUri(), previewImg.getUri()));
                });
    }

    @EventMapping
    public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    final ContentProvider provider = event.getMessage().getContentProvider();
                    final DownloadedContent mp4;
                    if (provider.isExternal()) {
                        mp4 = new DownloadedContent(null, provider.getOriginalContentUrl());
                    } else {
                        mp4 = saveContent("mp4", responseBody);
                    }
                    reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
                });
    }

    @EventMapping
    public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
        // You need to install ffmpeg and ImageMagick.
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    final ContentProvider provider = event.getMessage().getContentProvider();
                    final DownloadedContent mp4;
                    final DownloadedContent previewImg;
                    if (provider.isExternal()) {
                        mp4 = new DownloadedContent(null, provider.getOriginalContentUrl());
                        previewImg = new DownloadedContent(null, provider.getPreviewImageUrl());
                    } else {
                        mp4 = saveContent("mp4", responseBody);
                        previewImg = createTempFile("jpg");
                        system("convert",
                               mp4.path + "[0]",
                               previewImg.path.toString());
                    }
                    reply(event.getReplyToken(),
                          new VideoMessage(mp4.getUri(), previewImg.uri));
                });
    }

    @EventMapping
    public void handleFileMessageEvent(MessageEvent<FileMessageContent> event) {
        this.reply(event.getReplyToken(),
                   new TextMessage(String.format("Received '%s'(%d bytes)",
                                                 event.getMessage().getFileName(),
                                                 event.getMessage().getFileSize())));
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("unfollowed this bot: {}", event);
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got followed event");
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Joined " + event.getSource());
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken,
                       "Got postback data " + event.getPostbackContent().getData() + ", param " + event
                               .getPostbackContent().getParams().toString());
    }

    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
    }

    @EventMapping
    public void handleMemberJoined(MemberJoinedEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got memberJoined message " + event.getJoined().getMembers()
                                                                      .stream().map(Source::getUserId)
                                                                      .collect(Collectors.joining(",")));
    }

    @EventMapping
    public void handleMemberLeft(MemberLeftEvent event) {
        log.info("Got memberLeft message: {}", event.getLeft().getMembers()
                                                    .stream().map(Source::getUserId)
                                                    .collect(Collectors.joining(",")));
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }

    private void reply(@NonNull String replyToken,
                       @NonNull List<Message> messages,
                       boolean notificationDisabled) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleHeavyContent(String replyToken, String messageId,
                                    Consumer<MessageContentResponse> messageConsumer) {
        final MessageContentResponse response;
        try {
            response = lineBlobClient.getMessageContent(messageId)
                                     .get();
        } catch (InterruptedException | ExecutionException e) {
            reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
            throw new RuntimeException(e);
        }
        messageConsumer.accept(response);
    }

    private void handleSticker(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(
                content.getPackageId(), content.getStickerId())
        );
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        final String text = content.getText();
        final String userId = event.getSource().getUserId();
        if (userId != null) {
            byteRe = enCrypt(userId,System.getenv("line.bot.channel-secret"));
            encrytStr = parseByte2HexStr(byteRe);
        }

        switch (text) {
            case "profile": {
                log.info("Invoking 'profile' command: source:{}",
                         event.getSource());
               if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(
                                            replyToken,
                                            Arrays.asList(new TextMessage("(from group)"),
                                                          new TextMessage(
                                                                  "User id: " + userId),
                                                          new TextMessage(
                                                                  "Encrypted User id: " + encrytStr),
                                                          new TextMessage(
                                                                  "Display name: " + profile.getDisplayName()),
                                                          new ImageMessage(profile.getPictureUrl(),
                                                                           profile.getPictureUrl()))
                                    );
                                });
                    } else {
                        lineMessagingClient
                                .getProfile(userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(
                                            replyToken,
                                            Arrays.asList(new TextMessage(
                                                                  "User id: " + userId),
                                                          new TextMessage(
                                                                  "Encrypted User id: " + encrytStr),
                                                          new TextMessage(
                                                                  "Display name: " + profile.getDisplayName()),
                                                          new TextMessage("Status message: "
                                                                          + profile.getStatusMessage()))
                                    );

                                });
                    }
                } else {
                    this.replyText(replyToken, "Bot can't use profile API without user ID");
                }
                break;
            }
            case "bind": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "確定要綁定嗎?",
                        new URIAction("是",
                            URI.create("https://stg.card.rakuten.com.tw/corp/bind/index.xhtml?uid="+encrytStr), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("帳號綁定", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "campaign": {
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(createUri("/static/icon/1122.jpg"), "【66金夏趴】樂天點數最高11%！", "於活動期間內，在樂天市場使用樂天信用卡購物累積滿額並登錄活動，即可獲得加碼11%樂天點數回饋！", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code=1122"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/1075.jpg"), "【E 起同樂 一起饗樂】", "指定類別消費享最高10%刷卡金回饋", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code=1075"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/1124.jpg"), "【蝦皮新手大禮包】領券即享首筆五折起 >>限量開搶中", "卡友於活動期間內首次註冊蝦皮會員，首購時輸入專屬優惠碼並使用樂天卡結帳，即享滿額現折NT$200！", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code=1124"), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("最新優惠", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "installment": {
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(createUri("/static/icon/1123.jpg"), "【抽Dyson】夏天到了！抽Dyson空氣清淨機，再享5%回饋", "申辦單筆消費分期6期以上，享5%刷卡金回饋，每戶最高可回饋1,500元刷卡金，再抽Dyson空氣清淨機！", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/members/campaign/cpn.xhtml?code=1123&uid="+encrytStr), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/1125.jpg"), "【500元回饋】分期一筆就享驚人回饋！", "活動期間內只要成功自動分期一筆且登錄就享500元刷卡金回饋！", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/members/campaign/cpn.xhtml?code=1125&uid="+encrytStr), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/1126.jpg"), "【樂天就甘心】申辦帳單分期請您吃免費霜淇淋再折100！", "活動期間內線上申辦帳單分期成功並登錄，享刷卡金NT100元回饋、再送全家Fami原味霜淇淋一支！", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/members/campaign/cpn.xhtml?code=1126&uid="+encrytStr), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("分期活動", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "japan": {
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(createUri("/static/icon/1059.jpg"), "逛日本MITSUI OUTLET PARK名牌輕鬆購！送購物優惠券及精美小禮!", "出示兌換券及台灣樂天信用卡,即可換取精美小禮及合作店家所提供的優惠券。", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code=1059"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/1087.jpg"), "BicCamera集團購物享最高免稅10%+7%OFF", "出示優惠券並刷台灣樂天信用卡，得享最高免稅10%+7%OFF。", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code=1087"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/972.jpg"), "松本清免稅門市最高享免稅10%+7％OFF!", "實體免稅門市購物，消費滿額享免稅10%+最高7%OFF。", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code=972"), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("日本優惠", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "merchant": {
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(createUri("/static/icon/d621.jpg"), "沐越Mu Viet越式料理", "每桌贈「青木瓜雞絲沙拉」乙份", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code=d621"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/d622.jpg"), "享鴨", "每桌贈「干貝絲翡翠炊蛋」乙份	", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code=d622"), null)
                                )),
                                new CarouselColumn(createUri("/static/icon/d522.jpg"), "青花驕麻辣鍋", "每桌贈美國牛培根乙份", Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code=d522"), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("國內特店", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "merchant_installment": {
                ImageCarouselTemplate imageCarouselTemplate = new ImageCarouselTemplate(
                        Arrays.asList(
                                new ImageCarouselColumn(createUri("/static/icon/store_33.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create("http://www.rakuten.com.tw/"), null)
                                ),
                                new ImageCarouselColumn(createUri("/static/icon/store_35.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create("https://www.momoshop.com.tw/"), null)
                                ),
                                new ImageCarouselColumn(createUri("/static/icon/store_140.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create("https://shopee.tw/"), null)
                                )
                        ));
                TemplateMessage templateMessage = new TemplateMessage("分期特約商家",
                                                                      imageCarouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "intro":
                this.reply(replyToken, new ExampleFlexMessageSupplier().get());
                break;
            case "corporate_card":
                this.reply(replyToken, ImagemapMessage
                        .builder()
                        .baseUrl(createUri("/static/rich"))
                        .altText("公司卡")
                        .baseSize(new ImagemapBaseSize(1040, 1040))
                        .actions(Arrays.asList(
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/product/cccard.xhtml#section1")
                                                 .area(new ImagemapArea(0, 0, 520, 520))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/product/cccard.xhtml#section2")
                                                 .area(new ImagemapArea(520, 0, 520, 520))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/product/cccard.xhtml#section3")
                                                 .area(new ImagemapArea(0, 520, 520, 520))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/product/cccard.xhtml#section4")
                                                 .area(new ImagemapArea(520, 520, 520, 520))
                                                 .build()
                        ))
                        .build());
                break;
            case "wifi":
                this.reply(replyToken, ImagemapMessage
                        .builder()
                        .baseUrl(createUri("/static/imagemap_video"))
                        .altText("日韓5天Wi-Fi吃到飽")
                        .baseSize(new ImagemapBaseSize(722, 1040))
                        .video(
                                ImagemapVideo.builder()
                                             .originalContentUrl(
                                                     createUri("/static/imagemap_video/originalContent.mp4"))
                                             .previewImageUrl(
                                                     createUri("/static/imagemap_video/previewImage.jpg"))
                                             .area(new ImagemapArea(40, 46, 952, 536))
                                             .externalLink(
                                                     new ImagemapExternalLink(
                                                             URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code=992"),
                                                             "查看詳情")
                                             )
                                             .build()
                        )
                        .actions(singletonList(
                                MessageImagemapAction.builder()
                                                     .text("日韓5天Wi-Fi吃到飽")
                                                     .area(new ImagemapArea(260, 600, 450, 86))
                                                     .build()
                        ))
                        .build());
                break;
            case "promotion": {
                URI imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "推廣",
                        "選單",
                        Arrays.asList(
                                new MessageAction("公司簡介",
                                                  "intro"),
                                new MessageAction("公司卡",
                                                  "corporate_card"),
                                new MessageAction("分期特約商家",
                                                  "merchant_installment"),
                                new MessageAction("日韓5天Wi-Fi吃到飽",
                                                  "wifi")
                        ));
                TemplateMessage templateMessage = new TemplateMessage("推廣", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "help": {
                URI imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "小幫手",
                        "選單",
                        Arrays.asList(
                                new MessageAction("帳號資訊",
                                                  "profile"),
                                new MessageAction("帳號綁定",
                                                  "bind"),
                                new URIAction("聯絡客服", URI.create("tel:0225168518"), null),
                                new URIAction("粉絲團", URI.create("https://www.facebook.com/twrakutencard/"), null)
                        ));
                TemplateMessage templateMessage = new TemplateMessage("小幫手", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "activate": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "立即開卡?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/activation/"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("立即開卡", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "resend": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "重寄申請書?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/application/resend.xhtml"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("申請書重寄", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "activate": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "補上傳文件?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/application/reupload.xhtml"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("文件補上傳", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "shortcut":
                this.reply(replyToken, new MessageWithQuickReplySupplier().get());
                break;
            case "bye": {
                Source source = event.getSource();
                if (source instanceof GroupSource) {
                    this.replyText(replyToken, "Leaving group");
                    lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
                } else if (source instanceof RoomSource) {
                    this.replyText(replyToken, "Leaving room");
                    lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
                } else {
                    this.replyText(replyToken, "Bot can't leave from 1:1 chat");
                }
                break;
            }
            case "no_notify":
                this.reply(replyToken,
                           singletonList(new TextMessage("This message is send without a push notification")),
                           true);
                break;
            case "icon":
                this.reply(replyToken,
                           TextMessage.builder()
                                      .text("Hello, I'm cat! Meow~")
                                      .sender(Sender.builder()
                                                    .name("Cat")
                                                    .iconUrl(createUri("/static/icon/cat.png"))
                                                    .build())
                                      .build());
                break;
            default:
                log.info("Returns message {}: {}", replyToken, text);
                this.replyText(
                        replyToken,
                        createResponseMessage(text)
                );
                break;
        }
    }

    public String createResponseMessage(String sendMessage) {

        if(MESSAGE_MAP.containsKey(sendMessage)) {
            return MESSAGE_MAP.get(sendMessage);
        } else {
            return  OTHER_MESSAGE;
        }
    }

    private static URI createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .scheme("https")
                                          .path(path).build()
                                          .toUri();
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
        log.info("Got content-type: {}", responseBody);

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.getStream(), outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID() + '.' + ext;
        Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    private static class DownloadedContent {
        Path path;
        URI uri;
    }
    
    public static byte[] enCrypt(String content,String strKey) throws Exception{  
        KeyGenerator keygen;         
        SecretKey desKey; 
        Cipher c;         
        byte[] cByte;  
        String str = content;  
        keygen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG") ;
        secureRandom.setSeed(strKey.getBytes());
        keygen.init(128, secureRandom);
        desKey = keygen.generateKey();        
        c = Cipher.getInstance("AES");  
        c.init(Cipher.ENCRYPT_MODE, desKey); 
        cByte = c.doFinal(str.getBytes("UTF-8"));  return cByte; 
    }

    public static String parseByte2HexStr(byte buf[]) {  
        StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < buf.length; i++) {  
            String hex = Integer.toHexString(buf[i] & 0xFF);  
            if (hex.length() == 1) { 
                hex = '0' + hex; 
            }  
            sb.append(hex.toUpperCase()); 
        } 
        return sb.toString(); 
    }  

}
