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
import java.util.ArrayList;
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
    List<String> list;
    private static final String CORE_VALUE_01_MESSAGE = "於台灣樂天信用卡公司官網線上申請並進行動態密碼驗證和他行信用卡資訊驗證，且同時註冊或登錄樂天市場會員資料後，上傳身分證正反面影本及財力證明以利進行審核。 若您有其他文件相關問題，煩請致電本公司24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)  \n由客服專員為您服務,謝謝您!";
    private static final String CORE_VALUE_02_MESSAGE = "於台灣樂天信用卡公司官網線上申請時若未進行他行信用卡資訊驗證，本公司會於次日寄出申請書給您。";
    private static final String CORE_VALUE_03_MESSAGE = "於台灣樂天信用卡公司官網進入申請書重寄，本公司會於次日寄出申請書給您。";
    private static final String CORE_VALUE_04_MESSAGE = "紙本申請書若超過14天未寄回，可於台灣樂天信用卡公司官網重新申請。";
    private static final String CORE_VALUE_05_MESSAGE = "若超過30天上傳文件仍未補齊，會視為取消申請，可於台灣樂天信用卡公司官網重新申請，並需準備身分證正反面影本及財力證明。";
    private static final String CORE_VALUE_06_MESSAGE = "請於台灣樂天信用卡公司官網重新申請，並需準備身分證正反面影本及財力證明。";
    private static final String CORE_VALUE_07_MESSAGE = "本公司採線上申辦的方式。";
    private static final String CORE_VALUE_08_MESSAGE = "審核過程若是需要補件，將發送郵件通知。";
    private static final String CORE_VALUE_09_MESSAGE = "審核結果僅依照客戶所附上之所有財力證明及客戶與所有銀行往來之信用紀錄/借貸金額/信用卡張數多寡，使用時間長短/收入比/負債比…等等原因綜合考量，很抱歉，此次未達核卡標準";
    private static final String CORE_VALUE_10_MESSAGE = "我們提供以下管道讓您啟用卡片的服務:1、樂天信用卡官網提供線上開卡服務。2、您亦可透過語音開卡專線，若您所在地電話號碼為6碼請撥打41-1111按100 #；7碼或8碼請撥：(02)412-1111按100 #。提醒您，正附卡需分別進行開卡才能消費。";
    private static final String CORE_VALUE_11_MESSAGE = "您可於周一至週五上班時間來電(02)2508-7218查詢申辦進度。";
    private static final String CORE_VALUE_12_MESSAGE = "本公司上班時間為早上9點至下午6點。";
    private static final String CORE_VALUE_13_MESSAGE = "您好，樂天信用卡目前無提供此服務 。 ";
    private static final String CORE_VALUE_14_MESSAGE = "在您持卡有效期間內樂天信用卡為免年費的。";
    private static final String CORE_VALUE_15_MESSAGE = "煩請您透過查詢專線(02)2508-7218 選擇語音服務即可轉接專人服務為您處理 。";
    private static final String CORE_VALUE_16_MESSAGE = "您可於周一至週五上班時間來電(02)2508-7218查詢進度。";
    private static final String CORE_VALUE_17_MESSAGE = "附卡申請人與正卡關係必須為：父母、配偶、子女、兄弟姊妹或配偶父母，且年滿十五歲以上；申請時請填寫正、附卡人資料，附上正、附卡人身分證正反影本，及正、附卡人簽名。";
    private static final String CORE_VALUE_18_MESSAGE = "樂天信用卡官網辦卡頁面或會員服務選擇索取附卡申請書後,我們會盡快寄出申請書給您! ";
    private static final String CORE_VALUE_19_MESSAGE = "詳情請參閱官網: https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code=950";
    private static final String CORE_VALUE_20_MESSAGE = "有關日本國外交易手續費活動請連結下列網址，點選最新優惠活動→國外交易手續費回饋  \nhttps://card.rakuten.com.tw/corp/campaign/?openExternalBrowser=1  \n或致電樂天信用卡24小時客服中心(02)2516-8518將由客服人員為您査詢說明";
    private static final String CORE_VALUE_21_MESSAGE = "日本國外交易手續費待日幣交易完成繳款後，次月以刷卡金方式回饋。";
    private static final String CORE_VALUE_22_MESSAGE = "本公司會於次月以刷卡金方式回饋。 ";
    private static final String CORE_VALUE_23_MESSAGE = "您要永久調高您的信用卡額度，只要您持卡滿9個月，繳款記錄正常，請致電本公司24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)  \n將由客服專員為您服務。";
    private static final String CORE_VALUE_24_MESSAGE = "台灣樂天信用卡公司所發行的信用卡最低額度為五萬元◦額度高低將依照申請人所附的資料決定◦";
    private static final String CORE_VALUE_25_MESSAGE = "不好意思，目前申辦樂天信用卡一個人只能申請一種卡別。";
    private static final String CORE_VALUE_26_MESSAGE = "本公司部分活動是採登錄制 。";
    private static final String CORE_VALUE_27_MESSAGE = "公司每季（1月、4月、7月、10月）定期依本公司之持卡人卡片繳款記錄、持卡人卡片使用情形及聯合徵信中心之債信記錄、負債情形、授信、強制停卡或拒絶往來等信用紀錄為綜合評分，並考量本公司資金成本、營運成本（含營運利潤）等訂定持卡人循環信用利率差別定價（以下簡稱「差別利率」）。";
    private static final String CORE_VALUE_28_MESSAGE = "持台灣樂天信用卡及預借現金密碼至全球貼有Visa、MasterCard、JCB商標，或國內貼有NCCCNET梅花閃電標誌的自動櫃員機(ATM) 預借現金。若無預借現金密碼之客戶，可撥打本公司24小時客服專線(02)2516-8518、0800-505-058 (限市話)申請預借現金密碼。預借現金手續費為每筆預借現金X 3.5%+ NT$ 150。其他相關細節請見本公司官網:  https://card.rakuten.com.tw/corp/finance/#sec02?openExternalBrowser=1";
    private static final String CORE_VALUE_29_MESSAGE = "您可以選擇下列的任一種方式繳款：  \n・e-BILL 全國繳費網  \n・使用全省華南銀行及郵局自動扣繳或臨櫃繳款  \n・自動櫃員機轉帳/繳費  \n・全省7-ELEVEN便利商店繳款  \n 於2~3個工作天後可於「信用卡會員服務」查詢。";
    private static final String CORE_VALUE_30_MESSAGE = "您可至樂天信用卡官網下載自動扣款的授權書或致電我們24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)  \n由客服專員為您服務.謝謝您!";
    private static final String CORE_VALUE_31_MESSAGE = "您可至樂天信用卡官網下載取消轉帳扣款授權書，或來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您服務。";
    private static final String CORE_VALUE_32_MESSAGE = "請於https://card.rakuten.com.tw/auth/ \n進行新卡友註冊。";
    private static final String CORE_VALUE_33_MESSAGE = "樂天信用卡安全認證帳號是您在登入使用會員服務系統時所使用的帳號，與您本來持有的樂天會員帳號不同。透過此帳號的設定可以加強您使用信用卡會員服務系統的安全性，提供您更具安全防護之環境, 並保障您的帳戶安全。 設定格式需為6~32位英、數字混合，不可含有符號如_ - . ^ $ [ ] * + ? | ( ) ! # % & = @ ; : < > 。 ";
    private static final String CORE_VALUE_34_MESSAGE = "請於https://card.rakuten.com.tw/auth/ \n進行忘記帳號/忘記密碼。";
    private static final String CORE_VALUE_35_MESSAGE = "新戶於活動期間內申辦樂天信用卡正卡，核卡後於指定時間內，不限金額首次刷樂天信用卡，即可享租用《Horizon-WiFi》日本行動上網分享器5日免費優惠。";
    private static final String CORE_VALUE_36_MESSAGE = "於樂天市場的消費將會轉為樂天點數回饋於您的樂天帳號";
    private static final String CORE_VALUE_37_MESSAGE = "本公司有最新優惠，日本優惠，國內特店等優惠";
    private static final String CORE_VALUE_38_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/campaign/";
    private static final String CORE_VALUE_39_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/japan-benefit/";
    private static final String CORE_VALUE_40_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/merchant/";
    private static final String CORE_VALUE_41_MESSAGE = "正卡持卡人可登入「信用卡會員服務」申辦電子帳單服務。或於線上服務選單之各項表單下載專區，下載電子帳單申請單，正卡持卡人填妥後寄回本公司。 ";
    private static final String CORE_VALUE_42_MESSAGE = "紙本帳單是採平信寄出，若您在繳款日前仍未收到帳單，請致電本公司24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)。";
    private static final String CORE_VALUE_43_MESSAGE = "有關保險相關事宜，提供您保險公司免付費電話0800-088-800 (24hrs)，煩請逕行確認，謝謝。  \n活動官網： https://card.rakuten.com.tw/corp/product/insurance.xhtml?openExternalBrowser=1 ";
    private static final String CORE_VALUE_44_MESSAGE = "請稍後再次嘗試，若仍舊未改善，請電我們24小時客服專線(02)2516-8518  0800-505-058(限市話) 由客服專員為您服務.謝謝您!";
    private static final String CORE_VALUE_45_MESSAGE = "您可於https://card.rakuten.com.tw/members/statement/billed/ \n查看繳費方式和繳費期限";
    private static final String CORE_VALUE_46_MESSAGE = "您可於https://card.rakuten.com.tw/members/statement/unbilled/ \n查看交易明細";
    private static final String CORE_VALUE_47_MESSAGE = "您可於ATM輸入銀行代號008 華南銀行 和銷帳編號9519801150767980進行匯款，繳款記錄可於2至3個工作天後於https://card.rakuten.com.tw/members/statement/history/ \n查詢";
    private static final String CORE_VALUE_48_MESSAGE = "如果您對消費有疑問，本公司將代您向收單機構申請調閱簽帳單影本供您核對。並收取調閱簽單費用每筆新臺幣100元。";
    private static final String CORE_VALUE_49_MESSAGE = "有可能是您的信用卡尚未開卡、卡片晶片問題或可用額度不夠…等，詳細情況請您致電樂天信用卡24小時客服中心 (02)2516-8518將由客服人員為您査詢說明。 ";
    private static final String CORE_VALUE_50_MESSAGE = "卡片可於日本樂天市場消費，所獲得的日本樂天點數可轉換為台灣樂天點數，詳情請參閱https://point.rakuten.com.tw/";
    private static final String CORE_VALUE_51_MESSAGE = "1.常に前向きに行動しよう";
    private static final String CORE_VALUE_52_MESSAGE = "2.何事にも一生懸命、一所懸命やろう";
    private static final String CORE_VALUE_53_MESSAGE = "3.日本一のマナーを実践しよう";
    private static final String CORE_VALUE_54_MESSAGE = "4.NO.1をつくろう";
    private static final String CORE_VALUE_55_MESSAGE = "5.スピードを追求し喜びを与えよう";
    private static final String CORE_VALUE_56_MESSAGE = "6.全ての人にワクワクを仕掛けよう";
    private static final String CORE_VALUE_57_MESSAGE = "7.自分力を磨こう";
    private static final String CORE_VALUE_58_MESSAGE = "8.素直で謙虚になろう";
    private static final String CORE_VALUE_59_MESSAGE = "9.家族のようなチームをつくろう";
    private static final String CORE_VALUE_60_MESSAGE = "10.夢・希望を強く思い続け現実にしよう";

    private static final String OTHER_MESSAGE = "若您的問題尚未獲得解答 \n煩請致電本公司24小時客服專線 \n(02)2516-8518 \n0800-505-058(限市話) \n由客服專員為您服務,謝謝您!";
    
    private static final Map<String, String> MESSAGE_MAP = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("辦卡", CORE_VALUE_01_MESSAGE);
            put("申辦信用卡", CORE_VALUE_01_MESSAGE);
            put("申請信用卡", CORE_VALUE_01_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP2 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("信用卡申請書", CORE_VALUE_02_MESSAGE);
            put("申請書", CORE_VALUE_02_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP3 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("沒收到申請書", CORE_VALUE_03_MESSAGE);
            put("補寄", CORE_VALUE_03_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP4 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申請次數超過限制", CORE_VALUE_04_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP5 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("再申請", CORE_VALUE_05_MESSAGE);
            put("未回復", CORE_VALUE_05_MESSAGE);
            put("重新申請", CORE_VALUE_05_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP6 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申請書還可用嗎", CORE_VALUE_06_MESSAGE);
            put("申請單還可以使用嗎", CORE_VALUE_06_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP7 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申辦地點", CORE_VALUE_07_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP8 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("補件", CORE_VALUE_08_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP9 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("沒有通過", CORE_VALUE_09_MESSAGE);
            put("無法通過", CORE_VALUE_09_MESSAGE);
            put("沒有核過", CORE_VALUE_09_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP10 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("開卡", CORE_VALUE_10_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP11 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("電話打不進去", CORE_VALUE_11_MESSAGE);
            put("忙線", CORE_VALUE_11_MESSAGE);
            put("專人", CORE_VALUE_11_MESSAGE);
            put("客服專線", CORE_VALUE_11_MESSAGE);
            put("客服電話", CORE_VALUE_11_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP12 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("上班時間", CORE_VALUE_12_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP13 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("機場停車", CORE_VALUE_13_MESSAGE);
            put("免費巴士", CORE_VALUE_13_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP14 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("年費", CORE_VALUE_14_MESSAGE);     
         }
    });
    private static final Map<String, String> MESSAGE_MAP15 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申請進度", CORE_VALUE_15_MESSAGE);
            put("財力證明", CORE_VALUE_15_MESSAGE);
            put("辦卡資格", CORE_VALUE_15_MESSAGE);
            put("辦卡進度", CORE_VALUE_15_MESSAGE);
            put("審核", CORE_VALUE_15_MESSAGE);
            put("申辦進度", CORE_VALUE_15_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP16 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("已寄出嗎", CORE_VALUE_16_MESSAGE);
            put("幾天會收到", CORE_VALUE_16_MESSAGE);
            put("什麼時候會寄", CORE_VALUE_16_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP17 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("附卡", CORE_VALUE_17_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP18 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("附卡申請書", CORE_VALUE_18_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP19 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("貴賓室", CORE_VALUE_19_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP20 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("國外交易手續費", CORE_VALUE_20_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP21 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("手續費回饋", CORE_VALUE_21_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP22 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("現金回饋", CORE_VALUE_22_MESSAGE);
         }
    });
   private static final Map<String, String> MESSAGE_MAP23 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("提高信用卡額度", CORE_VALUE_23_MESSAGE);
            put("調高額度", CORE_VALUE_23_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP24 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("查詢信用卡額度", CORE_VALUE_24_MESSAGE);       
         }
    });
    private static final Map<String, String> MESSAGE_MAP25 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("再申辦", CORE_VALUE_25_MESSAGE);
            put("另外申請", CORE_VALUE_25_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP26 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("活動已登入", CORE_VALUE_26_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP27 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("循環利率", CORE_VALUE_27_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP28 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("預借現金", CORE_VALUE_28_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP29 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("繳款", CORE_VALUE_29_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP30 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("自動扣款", CORE_VALUE_30_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP31 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消轉帳扣款", CORE_VALUE_31_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP32 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("無法註冊會員", CORE_VALUE_32_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP33 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申辦帳號", CORE_VALUE_33_MESSAGE);
            put("樂天信用卡認證", CORE_VALUE_33_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP34 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("忘記帳號", CORE_VALUE_34_MESSAGE);
            put("忘記密碼", CORE_VALUE_34_MESSAGE);
            put("修改帳號", CORE_VALUE_34_MESSAGE);
            put("修改密碼", CORE_VALUE_34_MESSAGE);          
         }
    });
    private static final Map<String, String> MESSAGE_MAP35 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("WiFi", CORE_VALUE_35_MESSAGE);
            put("Wi-Fi", CORE_VALUE_35_MESSAGE);
            put("wifi", CORE_VALUE_35_MESSAGE);
            put("Wifi", CORE_VALUE_35_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP36 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("樂天帳號", CORE_VALUE_36_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP37 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("有什麼優惠", CORE_VALUE_37_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP38 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("最新優惠", CORE_VALUE_38_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP39 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("日本優惠", CORE_VALUE_39_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP40 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("國內特店", CORE_VALUE_40_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP41 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("電子帳單", CORE_VALUE_41_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP42 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("紙本帳單", CORE_VALUE_42_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP43 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("旅遊平安險", CORE_VALUE_43_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP44 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("無法登入", CORE_VALUE_44_MESSAGE);         
         }
    });
    private static final Map<String, String> MESSAGE_MAP45 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("繳費方式", CORE_VALUE_45_MESSAGE);
            put("繳費期限", CORE_VALUE_45_MESSAGE);
            put("繳款方式", CORE_VALUE_45_MESSAGE);
            put("繳款期限", CORE_VALUE_45_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP46 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("未入帳", CORE_VALUE_46_MESSAGE);
            put("未出帳", CORE_VALUE_46_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP47 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("匯款", CORE_VALUE_47_MESSAGE);
            put("轉帳", CORE_VALUE_47_MESSAGE);
            put("繳費紀錄", CORE_VALUE_47_MESSAGE);
            put("繳款紀錄", CORE_VALUE_47_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP48 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("交易明細", CORE_VALUE_48_MESSAGE);
            put("消費明細", CORE_VALUE_48_MESSAGE);
            put("交易問題", CORE_VALUE_48_MESSAGE);
            put("消費問題", CORE_VALUE_48_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP49 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("無法在日本刷卡", CORE_VALUE_49_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP50 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("日本的樂天購物網站", CORE_VALUE_50_MESSAGE);
            put("日本樂天購物網站", CORE_VALUE_50_MESSAGE);
            put("日本樂天市場", CORE_VALUE_50_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP51 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue1", CORE_VALUE_51_MESSAGE);
            put("COREVALUE1", CORE_VALUE_51_MESSAGE);
            put("こあばりゅー1", CORE_VALUE_51_MESSAGE);
            put("コアバリュー1", CORE_VALUE_51_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP52 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue2", CORE_VALUE_52_MESSAGE);
            put("COREVALUE2", CORE_VALUE_52_MESSAGE);
            put("こあばりゅー2", CORE_VALUE_52_MESSAGE);
            put("コアバリュー2", CORE_VALUE_52_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP53 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue3", CORE_VALUE_53_MESSAGE);
            put("COREVALUE3", CORE_VALUE_53_MESSAGE);
            put("こあばりゅー3", CORE_VALUE_53_MESSAGE);
            put("コアバリュー3", CORE_VALUE_53_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP54 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue4", CORE_VALUE_54_MESSAGE);
            put("COREVALUE4", CORE_VALUE_54_MESSAGE);
            put("こあばりゅー4", CORE_VALUE_54_MESSAGE);
            put("コアバリュー4", CORE_VALUE_54_MESSAGE);          
         }
    });
    private static final Map<String, String> MESSAGE_MAP55 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue5", CORE_VALUE_55_MESSAGE);
            put("COREVALUE5", CORE_VALUE_55_MESSAGE);
            put("こあばりゅー5", CORE_VALUE_55_MESSAGE);
            put("コアバリュー5", CORE_VALUE_55_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP56 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue6", CORE_VALUE_56_MESSAGE);
            put("COREVALUE6", CORE_VALUE_56_MESSAGE);
            put("こあばりゅー6", CORE_VALUE_56_MESSAGE);
            put("コアバリュー6", CORE_VALUE_56_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP57 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue7", CORE_VALUE_57_MESSAGE);
            put("COREVALUE7", CORE_VALUE_57_MESSAGE);
            put("こあばりゅー7", CORE_VALUE_57_MESSAGE);
            put("コアバリュー7", CORE_VALUE_57_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP58 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue8", CORE_VALUE_58_MESSAGE);
            put("COREVALUE8", CORE_VALUE_58_MESSAGE);
            put("こあばりゅー8", CORE_VALUE_58_MESSAGE);
            put("コアバリュー8", CORE_VALUE_58_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP59 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue9", CORE_VALUE_59_MESSAGE);
            put("COREVALUE9", CORE_VALUE_59_MESSAGE);
            put("こあばりゅー9", CORE_VALUE_59_MESSAGE);
            put("コアバリュー9", CORE_VALUE_59_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP60 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("corevalue10", CORE_VALUE_60_MESSAGE);
            put("COREVALUE10", CORE_VALUE_60_MESSAGE);
            put("こあばりゅー10", CORE_VALUE_60_MESSAGE);
            put("コアバリュー10", CORE_VALUE_60_MESSAGE);
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
            case "member": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "進入會員服務?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/auth/"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("會員服務", confirmTemplate);
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
            case "reupload": {
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
        list = new ArrayList<String>(MESSAGE_MAP.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_01_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP2.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_02_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP3.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_03_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP4.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_04_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP5.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_05_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP6.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_06_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP7.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_07_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP8.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_08_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP9.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_09_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP10.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_10_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP11.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_11_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP12.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_12_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP13.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_13_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP14.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_14_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP15.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_15_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP16.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_16_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP17.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_17_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP18.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_18_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP19.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_19_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP20.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_20_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP21.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_21_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP22.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_22_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP23.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_23_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP24.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_24_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP25.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_25_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP26.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_26_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP27.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_27_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP28.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_28_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP29.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_29_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP30.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_30_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP31.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_31_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP32.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_32_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP33.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_33_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP34.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_34_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP35.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_35_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP36.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_36_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP37.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_37_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP38.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_38_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP39.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_39_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP40.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_40_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP41.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_41_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP42.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_42_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP43.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_43_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP44.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_44_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP45.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_45_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP46.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_46_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP47.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_47_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP48.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_48_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP49.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_49_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP50.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_50_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP51.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_51_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP52.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_52_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP53.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_53_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP54.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_54_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP55.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_55_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP56.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_56_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP57.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_57_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP58.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_58_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP59.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_59_MESSAGE;
            }
        }
        list = new ArrayList<String>(MESSAGE_MAP60.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                return CORE_VALUE_60_MESSAGE;
            }
        }
        return  OTHER_MESSAGE;
        /*if(MESSAGE_MAP.containsKey(sendMessage)) {
            return MESSAGE_MAP.get(sendMessage);
        } else {
            return  OTHER_MESSAGE;
        }*/
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
