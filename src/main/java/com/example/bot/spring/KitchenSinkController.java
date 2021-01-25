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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.X509HostnameVerifier;

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
    private String responseText = "";
    private String flag = "";
    List<String> list;
    private static final String CORE_VALUE_01_MESSAGE = "於台灣樂天信用卡公司官網線上申請並進行動態密碼驗證和他行信用卡資訊驗證，且同時註冊或登錄樂天市場會員資料後，上傳身分證正反面影本及財力證明以利進行審核。 若您有其他文件相關問題，煩請致電本公司24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)  \n由客服專員為您服務,謝謝您!";
    private static final String CORE_VALUE_02_MESSAGE = "於台灣樂天信用卡公司官網線上申請時若未進行他行信用卡資訊驗證，本公司會於次日寄出申請書給您。";
    private static final String CORE_VALUE_03_MESSAGE = "於台灣樂天信用卡公司官網進入申請書重寄，本公司會於次日寄出申請書給您。";
    private static final String CORE_VALUE_04_MESSAGE = "紙本申請書若超過14天未寄回，可於台灣樂天信用卡公司官網重新申請。";
    private static final String CORE_VALUE_05_MESSAGE = "若超過30天上傳文件仍未補齊，會視為取消申請，可於台灣樂天信用卡公司官網重新申請，並需準備身分證正反面影本及財力證明。";
    private static final String CORE_VALUE_06_MESSAGE = "請於台灣樂天信用卡公司官網重新申請，並需準備身分證正反面影本及財力證明。";
    private static final String CORE_VALUE_07_MESSAGE = "本公司採線上申辦的方式。";
    private static final String CORE_VALUE_08_MESSAGE = "審核過程若是需要補件，將發送郵件通知。";
    private static final String CORE_VALUE_09_MESSAGE = "審核結果僅依照客戶所附上之所有財力證明及客戶與所有銀行往來之信用紀錄/借貸金額/信用卡張數多寡，使用時間長短/收入比/負債比…等等原因綜合考量，很抱歉，此次未達核卡標準。";
    private static final String CORE_VALUE_10_MESSAGE = "我們提供以下管道讓您啟用卡片的服務:1、樂天信用卡官網提供線上開卡服務。2、您亦可透過語音開卡專線，若您所在地電話號碼為6碼請撥打41-1111按100 #；7碼或8碼請撥：(02)412-1111按100 #。提醒您，正附卡需分別進行開卡才能消費。";
    private static final String CORE_VALUE_11_MESSAGE = "您可於周一至週五上班時間來電(02)2508-7218查詢申辦進度。";
    private static final String CORE_VALUE_12_MESSAGE = "本公司上班時間為早上9點至下午6點。";
    private static final String CORE_VALUE_13_MESSAGE = "您好，樂天信用卡目前無提供此服務 。 ";
    private static final String CORE_VALUE_14_MESSAGE = "在您持卡有效期間內樂天信用卡為免年費的。";
    private static final String CORE_VALUE_15_MESSAGE = "煩請您透過查詢專線(02)2508-7218 選擇語音服務即可轉接專人服務為您處理 。";
    private static final String CORE_VALUE_16_MESSAGE = "您可於周一至週五上班時間來電(02)2508-7218查詢進度。";
    private static final String CORE_VALUE_17_MESSAGE = "附卡申請人與正卡關係必須為：父母、配偶、子女、兄弟姊妹或配偶父母，且年滿十五歲以上；申請時請填寫正、附卡人資料，附上正、附卡人身分證正反影本，及正、附卡人簽名。";
    private static final String CORE_VALUE_18_MESSAGE = "樂天信用卡官網辦卡頁面或會員服務選擇索取附卡申請書後,我們會盡快寄出申請書給您! ";
    private static final String CORE_VALUE_19_MESSAGE = "詳情請參閱官網: https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code=950 ";
    private static final String CORE_VALUE_20_MESSAGE = "有關日本國外交易手續費活動請連結下列網址，點選最新優惠活動→國外交易手續費回饋  \nhttps://card.rakuten.com.tw/corp/campaign/?openExternalBrowser=1  \n或致電樂天信用卡24小時客服中心(02)2516-8518將由客服人員為您査詢說明。";
    private static final String CORE_VALUE_21_MESSAGE = "日本國外交易手續費待日幣交易完成繳款後，次月以刷卡金方式回饋。";
    private static final String CORE_VALUE_22_MESSAGE = "本公司會於次月以刷卡金方式回饋。";
    private static final String CORE_VALUE_23_MESSAGE = "， 請於https://card.rakuten.com.tw/members/credit-limit-change/ \n辦理。";
    private static final String CORE_VALUE_24_MESSAGE = "台灣樂天信用卡公司所發行的信用卡最低額度為五萬元◦額度高低將依照申請人所附的資料決定。";
    private static final String CORE_VALUE_25_MESSAGE = "不好意思，目前申辦樂天信用卡一個人只能申請一種卡別。";
    private static final String CORE_VALUE_26_MESSAGE = "本公司部分活動是採登錄制 。";
    private static final String CORE_VALUE_27_MESSAGE = "公司每季（1月、4月、7月、10月）定期依本公司之持卡人卡片繳款記錄、持卡人卡片使用情形及聯合徵信中心之債信記錄、負債情形、授信、強制停卡或拒絶往來等信用紀錄為綜合評分，並考量本公司資金成本、營運成本（含營運利潤）等訂定持卡人循環信用利率差別定價（以下簡稱「差別利率」）。";
    private static final String CORE_VALUE_28_MESSAGE = "持台灣樂天信用卡及預借現金密碼至全球貼有Visa、MasterCard、JCB商標，或國內貼有NCCCNET梅花閃電標誌的自動櫃員機(ATM) 預借現金。若無預借現金密碼之客戶，可撥打本公司24小時客服專線(02)2516-8518、0800-505-058 (限市話)申請預借現金密碼。預借現金手續費為每筆預借現金X 3.5%+ NT$ 150。其他相關細節請見本公司官網:  https://card.rakuten.com.tw/corp/finance/#sec02?openExternalBrowser=1 ";
    private static final String CORE_VALUE_29_MESSAGE = "您可以選擇下列的任一種方式繳款：  \n・e-BILL 全國繳費網  \n・使用全省華南銀行及郵局自動扣繳或臨櫃繳款  \n・自動櫃員機轉帳/繳費  \n・全省7-ELEVEN便利商店繳款  \n 於2~3個工作天後可於「信用卡會員服務」查詢。";
    private static final String CORE_VALUE_30_MESSAGE = "您可至樂天信用卡官網下載自動扣款的授權書或致電我們24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)  \n由客服專員為您服務.謝謝您!";
    private static final String CORE_VALUE_31_MESSAGE = "您可至樂天信用卡官網下載取消轉帳扣款授權書，或來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您服務。";
    private static final String CORE_VALUE_32_MESSAGE = "請於https://card.rakuten.com.tw/auth/ \n進行新卡友註冊。";
    private static final String CORE_VALUE_33_MESSAGE = "樂天信用卡安全認證帳號是您在登入使用會員服務系統時所使用的帳號，與您本來持有的樂天會員帳號不同。透過此帳號的設定可以加強您使用信用卡會員服務系統的安全性，提供您更具安全防護之環境, 並保障您的帳戶安全。 設定格式需為6~32位英、數字混合，不可含有符號如_ - . ^ $ [ ] * + ? | ( ) ! # % & = @ ; : < > 。";
    private static final String CORE_VALUE_34_MESSAGE = "請於https://card.rakuten.com.tw/auth/ \n進行忘記帳號/忘記密碼。";
    private static final String CORE_VALUE_35_MESSAGE = "新戶於活動期間內申辦樂天信用卡正卡，核卡後於指定時間內，不限金額首次刷樂天信用卡，即可享租用《Horizon-WiFi》日本行動上網分享器5日免費優惠。";
    private static final String CORE_VALUE_36_MESSAGE = "於樂天市場的消費將會轉為樂天點數回饋於您的樂天帳號。";
    private static final String CORE_VALUE_37_MESSAGE = "本公司有最新優惠，日本優惠，國內特店等優惠。";
    private static final String CORE_VALUE_38_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/campaign/ ";
    private static final String CORE_VALUE_39_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/japan-benefit/ ";
    private static final String CORE_VALUE_40_MESSAGE = "請參閱https://card.rakuten.com.tw/corp/merchant/ ";
    private static final String CORE_VALUE_41_MESSAGE = "正卡持卡人可登入「信用卡會員服務」申辦電子帳單服務。或於線上服務選單之各項表單下載專區，下載電子帳單申請單，正卡持卡人填妥後寄回本公司。";
    private static final String CORE_VALUE_42_MESSAGE = "紙本帳單是採平信寄出，若您在繳款日前仍未收到帳單，請致電本公司24小時客服專線  \n(02)2516-8518  \n0800-505-058(限市話)。";
    private static final String CORE_VALUE_43_MESSAGE = "有關保險相關事宜，提供您保險公司免付費電話0800-088-800 (24hrs)，煩請逕行確認，謝謝。  \n活動官網： https://card.rakuten.com.tw/corp/product/insurance.xhtml?openExternalBrowser=1 ";
    private static final String CORE_VALUE_44_MESSAGE = "請稍後再次嘗試，若仍舊未改善，請電我們24小時客服專線(02)2516-8518  0800-505-058(限市話) 由客服專員為您服務.謝謝您!";
    private static final String CORE_VALUE_45_MESSAGE = "您可於https://card.rakuten.com.tw/members/statement/billed/ \n查看繳費方式和繳費期限。";
    private static final String CORE_VALUE_46_MESSAGE = "您可於https://card.rakuten.com.tw/members/statement/unbilled/ \n查看交易明細。";
    private static final String CORE_VALUE_47_MESSAGE = "您可於ATM輸入銀行代號008 華南銀行 和銷帳編號9519801150767980進行匯款，繳款記錄可於2至3個工作天後於https://card.rakuten.com.tw/members/statement/history/ \n查詢。";
    private static final String CORE_VALUE_48_MESSAGE = "如果您對消費有疑問，本公司將代您向收單機構申請調閱簽帳單影本供您核對。並收取調閱簽單費用每筆新臺幣100元。";
    private static final String CORE_VALUE_49_MESSAGE = "有可能是您的信用卡尚未開卡、卡片晶片問題或可用額度不夠…等，詳細情況請您致電樂天信用卡24小時客服中心 (02)2516-8518將由客服人員為您査詢說明。";
    private static final String CORE_VALUE_50_MESSAGE = "卡片可於日本樂天市場消費，所獲得的日本樂天點數可轉換為台灣樂天點數，詳情請參閱https://point.rakuten.com.tw/ ";
    private static final String CORE_VALUE_51_MESSAGE = "當您在國外簽帳消費後，該帳款將以該清算日匯率（非簽帳日）加計國際組織之費用及本公司作業手續費後折算為新臺幣向您收取。";
    private static final String CORE_VALUE_52_MESSAGE = "煩請您透過查詢專線(02)2508-7218 選擇語音服務即可取消或轉接專人服務亦可為您處理 。";
    private static final String CORE_VALUE_53_MESSAGE = "您可連結至下列網址，點選常見問題→卡片申請→申請需附哪些文件。 https://card.rakuten.com.tw/corp/support/faq.xhtml?openExternalBrowser=1 \n或於週一至週五上班時間來電(02)2508-7218由客服專員為您服務。";
    private static final String CORE_VALUE_54_MESSAGE = "VISA ,MASTERCARD,JCB為隸屬於不同信用卡國際組織，各國際組織對該組織持卡卡友提供不同的權益及優惠，您可依您的喜好選擇申辦的卡別。";
    private static final String CORE_VALUE_55_MESSAGE = "不好意思，目前暫無繳款後發送簡訊及E-mail通知服務。";
    private static final String CORE_VALUE_56_MESSAGE = "待收到您的資料後若無需增補文件，我們會儘速為您處理，一般來說約7~15天的作業時間。";
    private static final String CORE_VALUE_57_MESSAGE = "我們有提供消費達台幣NTD3000(含)以上 簡訊的服務。";
    private static final String CORE_VALUE_58_MESSAGE = "煩請致電本公司24小時客服專線 \n(02)2516-8518 \n0800-505-058(限市話) \n由客服專員為您服務,謝謝您!";
    private static final String CORE_VALUE_59_MESSAGE = "台灣樂天信用卡公司所發行的信用卡最低額度為五萬元◦額度高低將依照申請人所附的資料決定。";
    private static final String CORE_VALUE_60_MESSAGE = "本公司帳單上回饋點數會在結帳日後15個工作天轉移到樂天市場，屆時您可在樂天市場點數樂部查詢點數入帳明細，麻煩您耐心等候。樂天點數查詢需請您在台灣樂天市場之「樂天點數俱樂部」上進行查詢。";
    private static final String CORE_VALUE_61_MESSAGE = "持台灣樂天信用卡及預借現金密碼至全球貼有Visa、MasterCard、JCB商標，或國內貼有NCCCNET梅花閃電標誌的自動櫃員機(ATM) 預借現金。若無預借現金密碼之客戶，可撥打本公司24小時客服專線(02)2516-8518、0800-505-058 (限市話)申請預借現金密碼。預借現金手續費為每筆預借現金X 3.5%+ NT$ 150。其他相關細節請見本公司官網:  https://card.rakuten.com.tw/corp/finance/#sec02?openExternalBrowser=1 ";
    private static final String CORE_VALUE_62_MESSAGE = "分期年利率為7.99%~11.99% \n請參考： https://card.rakuten.com.tw/corp/guide/payment.xhtml?openExternalBrowser=1 ";
    private static final String CORE_VALUE_63_MESSAGE = "煩請來信(客服信箱 customer_service@card.rakuten.com.tw)提供更名後新身分證影本與最近3個月戶籍謄本影本及英文護照拼音 (需與護照相同)重製卡手續費用新臺幣100元。並請致電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您服務。";
    private static final String CORE_VALUE_64_MESSAGE = "本行依客戶申請書所填之英文姓名製卡。如申請書內未填寫，本行將依據中文姓名直譯。如需變更請來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您提出申請。需請您提供護照英文拼音協助您更換卡片，惟辦理換卡將酌收新臺幣100元手續費。";
    private static final String CORE_VALUE_65_MESSAGE = "請來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您服務或請您剪斷卡片掛號寄回台北郵政第112-278號信箱(客服中心收)。";
    private static final String CORE_VALUE_66_MESSAGE = "您可在App Store / Google Play搜尋樂天信用卡進行下載。";
    private static final String CORE_VALUE_67_MESSAGE = "請聯繫信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話) ，將有專人為您查詢。";
    private static final String CORE_VALUE_68_MESSAGE = "請來信客服信箱: customer_service@card.rakuten.com.tw ，將有專人為您申請。";
    private static final String CORE_VALUE_69_MESSAGE = "如需辦理提前結清分期,請來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您服務。";
    private static final String CORE_VALUE_70_MESSAGE = "煩請您於週一至週五上班時間來電(02)2508-7218由客服專員為您服務。";
    private static final String CORE_VALUE_71_MESSAGE = "您可登入會員中心後至「帳務查詢」，本期帳單頁面可選擇產生7-ELEVEN繳款條碼，產生條碼後可持條碼至7-ELEVEN門市繳款。\n注意事項：\n1.每次產生條碼，條碼有效時間為三小時，請儘速於三小時內持該條碼至7-ELEVEN門市繳費。若超過三小時，請重新產生一次條碼。\n2.超商繳款金額以新台幣貳萬元為限。";
    private static final String CORE_VALUE_72_MESSAGE = "若欲取消電子帳單服務，請致電02-2516-8518、0800-505-058(限市話)將有專人為您服務。";
    private static final String CORE_VALUE_73_MESSAGE = "本公司將於結帳日後5日寄送信用卡電子帳單至您指定之電子信箱，提供連結登入至本公司會員服務查看信用卡帳單明細。為了避免信箱擋信之情形，可先將本公司電子郵件信箱設為您的聯絡人。";
    private static final String CORE_VALUE_74_MESSAGE = "當您的信用卡期限將屆，台灣樂天信用卡會自動於到期前約一個月，以掛號郵寄方式寄上新卡，您毋需辦理任何手續，但本公司保留卡片續發權利。";
    private static final String CORE_VALUE_75_MESSAGE = "樂天信用卡提供實體及電子帳單給持卡人，您的實體帳單於結帳日的3天後寄出，若您未能及時收到實體帳單建議您可登入樂天信用卡會員服務頁面查詢您的帳單明細。";
    private static final String CORE_VALUE_76_MESSAGE = "詳請參閱本公司官網: https://card.rakuten.com.tw/corp/disclosure/tax-fee.xhtml?openExternalBrowser=1 ";
    private static final String CORE_VALUE_77_MESSAGE = "本公司活動詳情皆載明正附卡獲贈資格，請正卡人進行活動登錄。";
    private static final String CORE_VALUE_78_MESSAGE = "當您有使用驗證機制做為安全驗證的網路平台購物時，台灣樂天信用卡將於交易開始時發送一組「交易動態密碼(One Time Password，OTP)」到您的手機，您必須於簡訊內容說明的有效時間內輸入該動態簡訊密碼，並成功送出方能完成該筆交易，以保障您網路購物權益並提升交易之安全性。";
    private static final String CORE_VALUE_79_MESSAGE = "有關結帳日變更，煩請聯絡信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058(限市話)，將有專人為您說明。";
    private static final String CORE_VALUE_80_MESSAGE = "您要調高您的臨時信用卡額度， 請於https://card.rakuten.com.tw/members/tmp-credit-limit-change/ \n辦理。";
    private static final String CORE_VALUE_81_MESSAGE = "有關樂天市場商品訂購及取消等問題, 請您致電樂天市場客服中心查詢樂天市場客服中心電話: (02) 2508-8181 ";
    private static final String CORE_VALUE_82_MESSAGE = "如您的信用卡有遺失、被竊或其他喪失占有等情形而通知本公司辦理掛失停用手續者，需付掛失手續費每卡新臺幣200元。請立即致電於本公司24小時客服專線：國內:請撥(02)2516-8518。國外：+886-2-25168518，24小時皆有客服專員為您解說如何辦理掛失及補發手續。";
    private static final String CORE_VALUE_83_MESSAGE = "自您辦理掛失手續的時間點往前推算24小時起，如果有被冒用所產生的損失，經查明不屬於持卡人的責任後，這些損失將會由本公司來承擔。如您是在掛失前被冒用之自付額以新台幣参千元整為上限。";
    private static final String CORE_VALUE_84_MESSAGE = "分期相關資訊請參閱本公司官網: https://card.rakuten.com.tw/corp/finance/?openExternalBrowser=1 ";
    private static final String CORE_VALUE_85_MESSAGE = "樂天信用卡是獨立的信用卡公司，目前沒有對外設立實體門市。";
    private static final String CORE_VALUE_86_MESSAGE = "若您欲取消該筆交易，需由商店執行取消該筆交易，並取得特約商店的退款證明。";
    private static final String CORE_VALUE_87_MESSAGE = "如果您是要求補寄非當期之帳單，除前二期之帳單免費外，本公司得按每月（份）帳單收取新臺幣100元之補寄帳單手續費。";
    private static final String CORE_VALUE_88_MESSAGE = "您可登入會員中心後至「帳務查詢」，本期帳單頁面可查詢到您的銷帳編號。";
    private static final String CORE_VALUE_89_MESSAGE = "國外實體商店刷卡無需輸入密碼，請店家改用列印簽單簽名的方式完成刷卡。";
    private static final String CORE_VALUE_90_MESSAGE = "如本期信用卡帳單已過繳款截止日，為避免逾期費用產生並影響您的用卡權益，請儘速於上班時間來電02-25087206查詢，如已繳款請無須理會。";
    private static final String CORE_VALUE_91_MESSAGE = "審核進度將依照客戶申請的數量多寡而有所增減，目前因申請量較大，收到申請案件後，整件/入件/審核處理時間(約7~15個工作天，不含假日)。";
    private static final String CORE_VALUE_92_MESSAGE = "請來電信用卡背面24小時客服專線: (02) 2516-8518或0800-505-058 (限市話)，將有專人為您服務。";
    private static final String CORE_VALUE_93_MESSAGE = "信用卡審核結果會發送郵件通知。";
    private static final String CORE_VALUE_94_MESSAGE = "歡迎於周一至週五上班時間來電(02)2508-7218查詢。";
    private static final String CORE_VALUE_95_MESSAGE = "詳情請參閱官網: https://card.rakuten.com.tw/corp/guide/balance.xhtml?openExternalBrowser=1 ";

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
            put("道路救援", CORE_VALUE_13_MESSAGE);
            put("機場接送", CORE_VALUE_13_MESSAGE);
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
            put("可用額度", CORE_VALUE_24_MESSAGE); 
         }
    });
    private static final Map<String, String> MESSAGE_MAP25 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("再申辦", CORE_VALUE_25_MESSAGE);
            put("另外申請", CORE_VALUE_25_MESSAGE);
            put("變更申請卡別", CORE_VALUE_25_MESSAGE);
            put("變更卡別", CORE_VALUE_25_MESSAGE);
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
            put("循環信用利率", CORE_VALUE_27_MESSAGE);
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
            put("旅平險", CORE_VALUE_43_MESSAGE);
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
            put("卡片無法刷", CORE_VALUE_49_MESSAGE);
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
            put("國外消費", CORE_VALUE_51_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP52 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消辦理信用卡", CORE_VALUE_52_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP53 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("準備的文件", CORE_VALUE_53_MESSAGE);
            put("準備文件", CORE_VALUE_53_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP54 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("卡別的差別", CORE_VALUE_54_MESSAGE);        
         }
    });
    private static final Map<String, String> MESSAGE_MAP55 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("繳款後會發送簡訊或EMAIL通知嗎", CORE_VALUE_55_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP56 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申請信用卡流程", CORE_VALUE_56_MESSAGE);
            put("申請流程", CORE_VALUE_56_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP57 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("刷卡簡訊", CORE_VALUE_57_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP58 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("修改個人資料", CORE_VALUE_58_MESSAGE);
            put("更改個人資料", CORE_VALUE_58_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP59 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("最高和最低的額度", CORE_VALUE_59_MESSAGE);
            put("最高和最低額度", CORE_VALUE_59_MESSAGE);
            put("最高額度", CORE_VALUE_59_MESSAGE);
            put("最低額度", CORE_VALUE_59_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP60 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("樂天點數", CORE_VALUE_60_MESSAGE);
         }
    });
        private static final Map<String, String> MESSAGE_MAP61 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("預借現金", CORE_VALUE_61_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP62 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("分期利息", CORE_VALUE_62_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP63 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("更改中文姓名", CORE_VALUE_63_MESSAGE);
            put("更改姓名", CORE_VALUE_63_MESSAGE);
            put("更改中文名字", CORE_VALUE_63_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP64 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("更改英文拼音", CORE_VALUE_64_MESSAGE);
            put("更改英文姓名", CORE_VALUE_64_MESSAGE);   
            put("更改英文名字", CORE_VALUE_64_MESSAGE);   
         }
    });
    private static final Map<String, String> MESSAGE_MAP65 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消信用卡", CORE_VALUE_65_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP66 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("樂天信用卡的APP", CORE_VALUE_66_MESSAGE);
            put("樂天信用卡APP", CORE_VALUE_66_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP67 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("沒收到免費WIFI序號", CORE_VALUE_67_MESSAGE);
            put("沒收到WIFI序號", CORE_VALUE_67_MESSAGE);
            put("沒收到wifi序號", CORE_VALUE_67_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP68 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消廣告活動EMAIL", CORE_VALUE_68_MESSAGE);
            put("取消EDM", CORE_VALUE_68_MESSAGE);
            put("取消edm", CORE_VALUE_68_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP69 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消分期", CORE_VALUE_69_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP70 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("更改寄送地址", CORE_VALUE_70_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP71 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("繳款條碼", CORE_VALUE_71_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP72 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消電子帳單", CORE_VALUE_72_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP73 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("收到電子帳單", CORE_VALUE_73_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP74 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("卡片如果到期", CORE_VALUE_74_MESSAGE);
            put("卡片到期", CORE_VALUE_74_MESSAGE);       
         }
    });
    private static final Map<String, String> MESSAGE_MAP75 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("收不到帳單", CORE_VALUE_75_MESSAGE);
            put("沒收到帳單", CORE_VALUE_75_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP76 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("代繳政府機構規費", CORE_VALUE_76_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP77 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("活動登錄", CORE_VALUE_77_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP78 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("網路交易安全驗證服務", CORE_VALUE_78_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP79 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("更改結帳週期", CORE_VALUE_79_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP80 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("申請調高臨時額度", CORE_VALUE_80_MESSAGE);
            put("額度不夠用", CORE_VALUE_80_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP81 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("樂天市場", CORE_VALUE_81_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP82 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("遺失", CORE_VALUE_82_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP83 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("掛失零風險", CORE_VALUE_83_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP84 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("分期方式", CORE_VALUE_84_MESSAGE);         
         }
    });
    private static final Map<String, String> MESSAGE_MAP85 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("樂天信用卡是銀行嗎", CORE_VALUE_85_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP86 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("取消交易", CORE_VALUE_86_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP87 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("補寄帳單", CORE_VALUE_87_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP88 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("銷帳編號", CORE_VALUE_88_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP89 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("國外刷卡密碼", CORE_VALUE_89_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP90 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("繳款簡訊", CORE_VALUE_90_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP91 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("急件處理", CORE_VALUE_91_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP92 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("重覆繳款", CORE_VALUE_92_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP93 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("審核結果", CORE_VALUE_93_MESSAGE);
         }
    });
    private static final Map<String, String> MESSAGE_MAP94 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("掛號編號", CORE_VALUE_94_MESSAGE);      
         }
    });
    private static final Map<String, String> MESSAGE_MAP95 = Collections.unmodifiableMap(new HashMap<String, String>(){
        private static final long serialVersionUID = 1L;
        {
            put("餘額代償", CORE_VALUE_95_MESSAGE);
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
        
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

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
                URL url = new URL("https://card.rakuten.com.tw/card-taiwan-app/rest/campaign-master");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Basic YXBwOnJha3V0ZW5jYXJk");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                int responseCode = conn.getResponseCode();
                String line;
                String responseData = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseData += line;
                }
                JsonArray list = Json.createReader(new StringReader(responseData)).readArray();
                String twCode1=list.getJsonObject(0).getString("campaignCode");
                String twName1=list.getJsonObject(0).getString("campaignName");
                if(twName1.length()>=60){
                    twName1=twName1.substring(0,60);
                }
                String twDesc1=list.getJsonObject(0).getString("campaignDescription");
                if(twDesc1.length()>=60){
                    twDesc1=twDesc1.substring(0,60);
                }
                String twCode2=list.getJsonObject(1).getString("campaignCode");
                String twName2=list.getJsonObject(1).getString("campaignName");
                if(twName2.length()>=60){
                    twName2=twName2.substring(0,60);
                }
                String twDesc2=list.getJsonObject(1).getString("campaignDescription");
                 if(twDesc2.length()>=60){
                    twDesc2=twDesc2.substring(0,60);
                 }
                String twCode3=list.getJsonObject(2).getString("campaignCode");
                String twName3=list.getJsonObject(2).getString("campaignName");
                if(twName3.length()>=60){
                    twName3=twName3.substring(0,60);
                }
                String twDesc3=list.getJsonObject(2).getString("campaignDescription");
                 if(twDesc3.length()>=60){
                    twDesc3=twDesc3.substring(0,60);
                }
                String twCode4=list.getJsonObject(3).getString("campaignCode");
                String twName4=list.getJsonObject(3).getString("campaignName");
                if(twName4.length()>=60){
                    twName4=twName4.substring(0,60);
                }
                String twDesc4=list.getJsonObject(3).getString("campaignDescription");
                 if(twDesc4.length()>=60){
                    twDesc4=twDesc4.substring(0,60);
                }
                String twCode5=list.getJsonObject(4).getString("campaignCode");
                String twName5=list.getJsonObject(4).getString("campaignName");
                if(twName5.length()>=60){
                    twName5=twName5.substring(0,60);
                }
                String twDesc5=list.getJsonObject(4).getString("campaignDescription");
                 if(twDesc5.length()>=60){
                    twDesc5=twDesc5.substring(0,60);
                }
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+twCode1+"/banner/710x310.jpg"), twName1, twDesc1, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+twCode1), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+twCode2+"/banner/710x310.jpg"), twName2, twDesc2, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+twCode2), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+twCode3+"/banner/710x310.jpg"), twName3, twDesc3, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+twCode3), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+twCode4+"/banner/710x310.jpg"), twName4, twDesc4, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+twCode4), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+twCode5+"/banner/710x310.jpg"), twName5, twDesc5, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+twCode5), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("最新優惠", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "installment": {
                URL url1 = new URL("https://card.rakuten.com.tw/card-taiwan-app/rest/campaign-master");
                HttpsURLConnection conn1 = (HttpsURLConnection) url1.openConnection();
                conn1.setRequestMethod("GET");
                conn1.setRequestProperty("Content-Type", "application/json");
                conn1.setRequestProperty("Authorization", "Basic YXBwOnJha3V0ZW5jYXJk");
                conn1.setDoOutput(true);
                conn1.setDoInput(true);
                int responseCode1 = conn1.getResponseCode();
                String line1;
                String responseData1 = "";
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
                while ((line1 = reader1.readLine()) != null) {
                    responseData1 += line1;
                }
                JsonArray list1 = Json.createReader(new StringReader(responseData1)).readArray();
                String inCode1 = "";
                String inName1 = "";
                String inDesc1 = "";
                String inCode2 = "";
                String inName2 = "";
                String inDesc2 = "";
                String inCode3 = "";
                String inName3 = "";
                String inDesc3 = "";
                int count = 1;
                for (int i=0;i<list1.size();i++) {
                    if(list1.getJsonObject(i).getString("campaignDescription").contains("分期")) {
                        switch(count) {
                            case 1:
                                inCode1=list1.getJsonObject(i).getString("campaignCode");
                                inName1=list1.getJsonObject(i).getString("campaignName");
                                if(inName1.length()>=60){
                                    inName1=inName1.substring(0,60);
                                }
                                inDesc1=list1.getJsonObject(i).getString("campaignDescription");
                                if(inDesc1.length()>=60){
                                    inDesc1=inDesc1.substring(0,60);
                                }
                                count++;
                                break;
                            case 2:
                                inCode2=list1.getJsonObject(i).getString("campaignCode");
                                inName2=list1.getJsonObject(i).getString("campaignName");
                                if(inName2.length()>=60){
                                    inName2=inName2.substring(0,60);
                                }
                                inDesc2=list1.getJsonObject(i).getString("campaignDescription");
                                if(inDesc2.length()>=60){
                                    inDesc2=inDesc2.substring(0,60);
                                }
                                count++;
                                break;
                            case 3:
                                inCode3=list1.getJsonObject(i).getString("campaignCode");
                                inName3=list1.getJsonObject(i).getString("campaignName");
                                if(inName3.length()>=60){
                                    inName3=inName3.substring(0,60);
                                }
                                inDesc3=list1.getJsonObject(i).getString("campaignDescription");
                                if(inDesc3.length()>=60){
                                    inDesc3=inDesc3.substring(0,60);
                                }
                                count++;
                                break;
                        }
                        
                    }
                }
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+inCode1+"/banner/710x310.jpg"), inName1, inDesc1, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+inCode1), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+inCode2+"/banner/710x310.jpg"), inName2, inDesc2, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+inCode2), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+inCode3+"/banner/710x310.jpg"), inName3, inDesc3, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code="+inCode3), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("分期活動", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "japan": {
                URL url2 = new URL("https://card.rakuten.com.tw/card-taiwan-app/rest/campaign-master/japan-benefit/all");
                HttpsURLConnection conn2 = (HttpsURLConnection) url2.openConnection();
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("Content-Type", "application/json");
                conn2.setRequestProperty("Authorization", "Basic YXBwOnJha3V0ZW5jYXJk");
                conn2.setDoOutput(true);
                conn2.setDoInput(true);
                int responseCode2 = conn2.getResponseCode();
                String line2;
                String responseData2 = "";
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                while ((line2 = reader2.readLine()) != null) {
                    responseData2 += line2;
                }
                JsonArray list2 = Json.createReader(new StringReader(responseData2)).readArray();
                String jpCode1=list2.getJsonObject(0).getString("campaignCode");
                String jpName1=list2.getJsonObject(0).getString("campaignName");
                if(jpName1.length()>=60){
                    jpName1=jpName1.substring(0,60);
                }
                String jpDesc1=list2.getJsonObject(0).getString("campaignDescription");
                if(jpDesc1.length()>=60){
                    jpDesc1=jpDesc1.substring(0,60);
                }
                String jpCode2=list2.getJsonObject(1).getString("campaignCode");
                String jpName2=list2.getJsonObject(1).getString("campaignName");
                if(jpName2.length()>=60){
                    jpName2=jpName2.substring(0,60);
                }
                String jpDesc2=list2.getJsonObject(1).getString("campaignDescription");
                if(jpDesc2.length()>=60){
                    jpDesc2=jpDesc2.substring(0,60);
                }
                String jpCode3=list2.getJsonObject(2).getString("campaignCode");
                String jpName3=list2.getJsonObject(2).getString("campaignName");
                if(jpName3.length()>=60){
                    jpName3=jpName3.substring(0,60);
                }
                String jpDesc3=list2.getJsonObject(2).getString("campaignDescription");
                if(jpDesc3.length()>=60){
                    jpDesc3=jpDesc3.substring(0,60);
                }
                String jpCode4=list2.getJsonObject(3).getString("campaignCode");
                String jpName4=list2.getJsonObject(3).getString("campaignName");
                if(jpName4.length()>=60){
                    jpName4=jpName4.substring(0,60);
                }
                String jpDesc4=list2.getJsonObject(3).getString("campaignDescription");
                if(jpDesc4.length()>=60){
                    jpDesc4=jpDesc4.substring(0,60);
                }
                String jpCode5=list2.getJsonObject(4).getString("campaignCode");
                String jpName5=list2.getJsonObject(4).getString("campaignName");
                if(jpName5.length()>=60){
                    jpName5=jpName5.substring(0,60);
                }
                String jpDesc5=list2.getJsonObject(4).getString("campaignDescription");
                if(jpDesc5.length()>=60){
                    jpDesc5=jpDesc5.substring(0,60);
                }
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+jpCode1+"/banner/710x310.jpg"), jpName1, jpDesc1, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code="+jpCode1), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+jpCode2+"/banner/710x310.jpg"), jpName2, jpDesc2, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code="+jpCode2), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+jpCode3+"/banner/710x310.jpg"), jpName3, jpDesc3, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code="+jpCode3), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+jpCode4+"/banner/710x310.jpg"), jpName4, jpDesc4, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code="+jpCode4), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/campaign/"+jpCode5+"/banner/710x310.jpg"), jpName5, jpDesc5, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/japan-benefit/store.xhtml?code="+jpCode5), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("日本優惠", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "merchant": {
                URL url3 = new URL("https://card.rakuten.com.tw/card-taiwan-app/rest/campaign-master/merchant/recommendation");
                HttpsURLConnection conn3 = (HttpsURLConnection) url3.openConnection();
                conn3.setRequestMethod("GET");
                conn3.setRequestProperty("Content-Type", "application/json");
                conn3.setRequestProperty("Authorization", "Basic YXBwOnJha3V0ZW5jYXJk");
                conn3.setDoOutput(true);
                conn3.setDoInput(true);
                int responseCode3 = conn3.getResponseCode();
                String line3;
                String responseData3 = "";
                BufferedReader reader3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
                while ((line3 = reader3.readLine()) != null) {
                    responseData3 += line3;
                }
                JsonArray list3 = Json.createReader(new StringReader(responseData3)).readArray();
                String meCode1=list3.getJsonObject(0).getString("campaignCode");
                String meName1=list3.getJsonObject(0).getString("campaignName");
                if(meName1.length()>=60){
                    meName1=meName1.substring(0,60);
                }
                String meDesc1=list3.getJsonObject(0).getString("campaignDescription");
                if(meDesc1.length()>=60){
                    meDesc1=meDesc1.substring(0,60);
                }
                String meCode2=list3.getJsonObject(1).getString("campaignCode");
                String meName2=list3.getJsonObject(1).getString("campaignName");
                if(meName2.length()>=60){
                    meName2=meName2.substring(0,60);
                }
                String meDesc2=list3.getJsonObject(1).getString("campaignDescription");
                if(meDesc2.length()>=60){
                    meDesc2=meDesc2.substring(0,60);
                }
                String meCode3=list3.getJsonObject(2).getString("campaignCode");
                String meName3=list3.getJsonObject(2).getString("campaignName");
                if(meName3.length()>=60){
                    meName3=meName3.substring(0,60);
                }
                String meDesc3=list3.getJsonObject(2).getString("campaignDescription");
                if(meDesc3.length()>=60){
                    meDesc3=meDesc3.substring(0,60);
                }
                String meCode4=list3.getJsonObject(3).getString("campaignCode");
                String meName4=list3.getJsonObject(3).getString("campaignName");
                if(meName4.length()>=60){
                    meName4=meName4.substring(0,60);
                }
                String meDesc4=list3.getJsonObject(3).getString("campaignDescription");
                if(meDesc4.length()>=60){
                    meDesc4=meDesc4.substring(0,60);
                }
                String meCode5=list3.getJsonObject(4).getString("campaignCode");
                String meName5=list3.getJsonObject(4).getString("campaignName");
                if(meName5.length()>=60){
                    meName5=meName5.substring(0,60);
                }
                String meDesc5=list3.getJsonObject(4).getString("campaignDescription");
                if(meDesc5.length()>=60){
                    meDesc5=meDesc5.substring(0,60);
                }
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant/"+meCode1+"/400x250.jpg"), meName1, meDesc1, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code="+meCode1), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant/"+meCode2+"/400x250.jpg"), meName2, meDesc2, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code="+meCode2), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant/"+meCode3+"/400x250.jpg"), meName3, meDesc3, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code="+meCode3), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant/"+meCode4+"/400x250.jpg"), meName4, meDesc4, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code="+meCode4), null)
                                )),
                                new CarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant/"+meCode5+"/400x250.jpg"), meName5, meDesc5, Arrays.asList(
                                        new URIAction("立即前往",
                                                      URI.create("https://card.rakuten.com.tw/corp/merchant/cpn.xhtml?code="+meCode5), null)
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("國內特店", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "merchant_installment": {
                URL url4 = new URL("https://card.rakuten.com.tw/card-taiwan-app/rest/merchant-installment");
                HttpsURLConnection conn4 = (HttpsURLConnection) url4.openConnection();
                conn4.setRequestMethod("GET");
                conn4.setRequestProperty("Content-Type", "application/json");
                conn4.setRequestProperty("Authorization", "Basic YXBwOnJha3V0ZW5jYXJk");
                conn4.setDoOutput(true);
                conn4.setDoInput(true);
                int responseCode4 = conn4.getResponseCode();
                String line4;
                String responseData4 = "";
                BufferedReader reader4 = new BufferedReader(new InputStreamReader(conn4.getInputStream()));
                while ((line4 = reader4.readLine()) != null) {
                    responseData4 += line4;
                }
                JsonArray list4 = Json.createReader(new StringReader(responseData4)).readArray();
                String miCode1=list4.getJsonObject(0).getString("merchantArea");
                String miUrl1=list4.getJsonObject(0).getString("merchantUrl");
                String miCode2=list4.getJsonObject(1).getString("merchantArea");
                String miUrl2=list4.getJsonObject(1).getString("merchantUrl");
                String miCode3=list4.getJsonObject(2).getString("merchantArea");
                String miUrl3=list4.getJsonObject(2).getString("merchantUrl");
                String miCode4=list4.getJsonObject(3).getString("merchantArea");
                String miUrl4=list4.getJsonObject(3).getString("merchantUrl");
                String miCode5=list4.getJsonObject(4).getString("merchantArea");
                String miUrl5=list4.getJsonObject(4).getString("merchantUrl");
                ImageCarouselTemplate imageCarouselTemplate = new ImageCarouselTemplate(
                        Arrays.asList(
                                new ImageCarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant_installment/"+miCode1+"/200x162.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create(miUrl1), null)
                                ),
                                new ImageCarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant_installment/"+miCode2+"/200x162.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create(miUrl2), null)
                                ),
                                new ImageCarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant_installment/"+miCode3+"/200x162.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create(miUrl3), null)
                                ),
                                new ImageCarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant_installment/"+miCode4+"/200x162.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create(miUrl4), null)
                                ),
                                new ImageCarouselColumn(new URI("https://image.card.tw.r10s.com/images/corp/merchant_installment/"+miCode5+"/200x162.jpg"),
                                                        new URIAction("立即前往",
                                                                      URI.create(miUrl5), null)
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
            case "您好，我是智能客服小樂。很高興為您服務，提供常用功能快速連結給您：":
                this.reply(replyToken, ImagemapMessage
                        .builder()
                        .baseUrl(createUri("/static/icon"))
                        .altText("智能客服")
                        .baseSize(new ImagemapBaseSize(1040, 810))
                        .actions(Arrays.asList(
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/application/status.xhtml")
                                                 .area(new ImagemapArea(0, 0, 347, 405))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/application/reupload.xhtml")
                                                 .area(new ImagemapArea(347, 0, 347, 405))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/activation/")
                                                 .area(new ImagemapArea(694, 0, 346, 405))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/campaign/")
                                                 .area(new ImagemapArea(0, 405, 347, 405))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/support/contact.xhtml")
                                                 .area(new ImagemapArea(347, 405, 347, 405))
                                                 .build(),
                                URIImagemapAction.builder()
                                                 .linkUri("https://card.rakuten.com.tw/corp/guide/new-app.xhtml")
                                                 .area(new ImagemapArea(694, 405, 346, 405))
                                                 .build()
                        ))
                        .build());
                break;
            case "wifi":
                this.reply(replyToken, ImagemapMessage
                        .builder()
                        .baseUrl(createUri("/static/imagemap_video"))
                        .altText("租借wifi好easy")
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
                                                             URI.create("https://card.rakuten.com.tw/corp/campaign/cpn.xhtml?code=1246"),
                                                             "查看詳情")
                                             )
                                             .build()
                        )
                        .actions(singletonList(
                                MessageImagemapAction.builder()
                                                     .text("租借wifi好easy")
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
                                new MessageAction("指定網購加碼",
                                                  "ecbonus")
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
                                new MessageAction("立即開卡",
                                                  "activate"),
                                new MessageAction("帳號綁定",
                                                  "bind"),
                                new URIAction("聯絡客服", URI.create("tel:0225168518"), null),
                                new URIAction("粉絲團", URI.create("https://www.facebook.com/twrakutencard/"), null)
                        ));
                TemplateMessage templateMessage = new TemplateMessage("小幫手", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "latest": {
                URI imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "卡友優惠",
                        "選單",
                        Arrays.asList(
                                new MessageAction("最新優惠",
                                                  "campaign"),
                                new MessageAction("分期活動",
                                                  "installment"),
                                new MessageAction("國內特店",
                                                  "merchant"),
                                new MessageAction("帳務查詢",
                                                  "bill")
                        ));
                TemplateMessage templateMessage = new TemplateMessage("卡友優惠", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "application": {
                URI imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "辦卡相關",
                        "選單",
                        Arrays.asList(
                                new MessageAction("重寄申請書",
                                                  "resend"),
                                new MessageAction("補上傳文件",
                                                  "reupload"),
                                new MessageAction("查詢進度",
                                                  "status"),
                                new MessageAction("推薦辦卡",
                                                  "mgm")
                        ));
                TemplateMessage templateMessage = new TemplateMessage("辦卡相關", buttonsTemplate);
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
            case "bill": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "進入帳務查詢?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/members/statement/billed/index.xhtml?uid="+encrytStr), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("帳務查詢", confirmTemplate);
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
            case "status": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "查詢進度?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/application/status.xhtml"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("查詢進度", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "mgm": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "推薦辦卡?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/application/mgm.xhtml"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("推薦辦卡", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "ecbonus": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "指定網購加碼?",
                        new URIAction("是",
                            URI.create("https://card.rakuten.com.tw/corp/ecbonus-list/"), null),
                        new MessageAction("否", "No")
                );
                TemplateMessage templateMessage = new TemplateMessage("指定網購加碼", confirmTemplate);
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
        responseText = "";
        list = new ArrayList<String>(MESSAGE_MAP.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "1";
                break;
            }
        }
        if("1".equals(flag)) {
            responseText = responseText + CORE_VALUE_01_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP2.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "2";
                break;
            }
        }
        if("2".equals(flag)) {
            responseText = responseText + CORE_VALUE_02_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP3.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "3";
                break;
            }
        }
        if("3".equals(flag)) {
            responseText = responseText + CORE_VALUE_03_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP4.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "4";
                break;
            }
        }
        if("4".equals(flag)) {
            responseText = responseText + CORE_VALUE_04_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP5.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "5";
                break;
            }
        }
        if("5".equals(flag)) {
            responseText = responseText + CORE_VALUE_05_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP6.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "6";
                break;
            }
        }
        if("6".equals(flag)) {
            responseText = responseText + CORE_VALUE_06_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP7.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "7";
                break;
            }
        }
        if("7".equals(flag)) {
            responseText = responseText + CORE_VALUE_07_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP8.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "8";
                break;
            }
        }
        if("8".equals(flag)) {
            responseText = responseText + CORE_VALUE_08_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP9.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "9";
                break;
            }
        }
        if("9".equals(flag)) {
            responseText = responseText + CORE_VALUE_09_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP10.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "10";
                break;
            }
        }
        if("10".equals(flag)) {
            responseText = responseText + CORE_VALUE_10_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP11.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "11";
                break;
            }
        }
        if("11".equals(flag)) {
            responseText = responseText + CORE_VALUE_11_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP12.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "12";
                break;
            }
        }
        if("12".equals(flag)) {
            responseText = responseText + CORE_VALUE_12_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP13.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "13";
                break;
            }
        }
        if("13".equals(flag)) {
            responseText = responseText + CORE_VALUE_13_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP14.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "14";
                break;
            }
        }
        if("14".equals(flag)) {
            responseText = responseText + CORE_VALUE_14_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP15.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "15";
                break;
            }
        }
        if("15".equals(flag)) {
            responseText = responseText + CORE_VALUE_15_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP16.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "16";
                break;
            }
        }
        if("16".equals(flag)) {
            responseText = responseText + CORE_VALUE_16_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP17.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "17";
                break;
            }
        }
        if("17".equals(flag)) {
            responseText = responseText + CORE_VALUE_17_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP18.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "18";
                break;
            }
        }
        if("18".equals(flag)) {
            responseText = responseText + CORE_VALUE_18_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP19.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "19";
                break;
            }
        }
        if("19".equals(flag)) {
            responseText = responseText + CORE_VALUE_19_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP20.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "20";
                break;
            }
        }
        if("20".equals(flag)) {
            responseText = responseText + CORE_VALUE_20_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP21.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "21";
                break;
            }
        }
        if("21".equals(flag)) {
            responseText = responseText + CORE_VALUE_21_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP22.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "22";
                break;
            }
        }
        if("22".equals(flag)) {
            responseText = responseText + CORE_VALUE_22_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP23.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "23";
                break;
            }
        }
        if("23".equals(flag)) {
            responseText = responseText + CORE_VALUE_23_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP24.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "24";
                break;
            }
        }
        if("24".equals(flag)) {
            responseText = responseText + CORE_VALUE_24_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP25.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "25";
                break;
            }
        }
        if("25".equals(flag)) {
            responseText = responseText + CORE_VALUE_25_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP26.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "26";
                break;
            }
        }
        if("26".equals(flag)) {
            responseText = responseText + CORE_VALUE_26_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP27.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "27";
                break;
            }
        }
        if("27".equals(flag)) {
            responseText = responseText + CORE_VALUE_27_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP28.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "28";
                break;
            }
        }
        if("28".equals(flag)) {
            responseText = responseText + CORE_VALUE_28_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP29.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "29";
                break;
            }
        }
        if("29".equals(flag)) {
            responseText = responseText + CORE_VALUE_29_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP30.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "30";
                break;
            }
        }
        if("30".equals(flag)) {
            responseText = responseText + CORE_VALUE_30_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP31.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "31";
                break;
            }
        }
        if("31".equals(flag)) {
            responseText = responseText + CORE_VALUE_31_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP32.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "32";
                break;
            }
        }
        if("32".equals(flag)) {
            responseText = responseText + CORE_VALUE_32_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP33.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "33";
                break;
            }
        }
        if("33".equals(flag)) {
            responseText = responseText + CORE_VALUE_33_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP34.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "34";
                break;
            }
        }
        if("34".equals(flag)) {
            responseText = responseText + CORE_VALUE_34_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP35.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "35";
                break;
            }
        }
        if("35".equals(flag)) {
            responseText = responseText + CORE_VALUE_35_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP36.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "36";
                break;
            }
        }
        if("36".equals(flag)) {
            responseText = responseText + CORE_VALUE_36_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP37.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "37";
                break;
            }
        }
        if("37".equals(flag)) {
            responseText = responseText + CORE_VALUE_37_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP38.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "38";
                break;
            }
        }
        if("38".equals(flag)) {
            responseText = responseText + CORE_VALUE_38_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP39.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "39";
                break;
            }
        }
        if("39".equals(flag)) {
            responseText = responseText + CORE_VALUE_39_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP40.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "40";
                break;
            }
        }
        if("40".equals(flag)) {
            responseText = responseText + CORE_VALUE_40_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP41.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "41";
                break;
            }
        }
        if("41".equals(flag)) {
            responseText = responseText + CORE_VALUE_41_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP42.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "42";
                break;
            }
        }
        if("42".equals(flag)) {
            responseText = responseText + CORE_VALUE_42_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP43.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "43";
                break;
            }
        }
        if("43".equals(flag)) {
            responseText = responseText + CORE_VALUE_43_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP44.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "44";
                break;
            }
        }
        if("44".equals(flag)) {
            responseText = responseText + CORE_VALUE_44_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP45.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "45";
                break;
            }
        }
        if("45".equals(flag)) {
            responseText = responseText + CORE_VALUE_45_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP46.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "46";
                break;
            }
        }
        if("46".equals(flag)) {
            responseText = responseText + CORE_VALUE_46_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP47.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "47";
                break;
            }
        }
        if("47".equals(flag)) {
            responseText = responseText + CORE_VALUE_47_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP48.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "48";
                break;
            }
        }
        if("48".equals(flag)) {
            responseText = responseText + CORE_VALUE_48_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP49.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "49";
                break;
            }
        }
        if("49".equals(flag)) {
            responseText = responseText + CORE_VALUE_49_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP50.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "50";
                break;
            }
        }
        if("50".equals(flag)) {
            responseText = responseText + CORE_VALUE_50_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP51.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "51";
                break;
            }
        }
        if("51".equals(flag)) {
            responseText = responseText + CORE_VALUE_51_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP52.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "52";
                break;
            }
        }
        if("52".equals(flag)) {
            responseText = responseText + CORE_VALUE_52_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP53.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "53";
                break;
            }
        }
        if("53".equals(flag)) {
            responseText = responseText + CORE_VALUE_53_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP54.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "54";
                break;
            }
        }
        if("54".equals(flag)) {
            responseText = responseText + CORE_VALUE_54_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP55.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "55";
                break;
            }
        }
        if("55".equals(flag)) {
            responseText = responseText + CORE_VALUE_55_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP56.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "56";
                break;
            }
        }
        if("56".equals(flag)) {
            responseText = responseText + CORE_VALUE_56_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP57.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "57";
                break;
            }
        }
        if("57".equals(flag)) {
            responseText = responseText + CORE_VALUE_57_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP58.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "58";
                break;
            }
        }
        if("58".equals(flag)) {
            responseText = responseText + CORE_VALUE_58_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP59.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "59";
                break;
            }
        }
        if("59".equals(flag)) {
            responseText = responseText + CORE_VALUE_59_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP60.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "60";
                break;
            }
        }
        if("60".equals(flag)) {
            responseText = responseText + CORE_VALUE_60_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP61.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "61";
                break;
            }
        }
        if("61".equals(flag)) {
            responseText = responseText + CORE_VALUE_61_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP62.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "62";
                break;
            }
        }
        if("62".equals(flag)) {
            responseText = responseText + CORE_VALUE_62_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP63.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "63";
                break;
            }
        }
        if("63".equals(flag)) {
            responseText = responseText + CORE_VALUE_63_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP64.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "64";
                break;
            }
        }
        if("64".equals(flag)) {
            responseText = responseText + CORE_VALUE_64_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP65.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "65";
                break;
            }
        }
        if("65".equals(flag)) {
            responseText = responseText + CORE_VALUE_65_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP66.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "66";
                break;
            }
        }
        if("66".equals(flag)) {
            responseText = responseText + CORE_VALUE_66_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP67.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "67";
                break;
            }
        }
        if("67".equals(flag)) {
            responseText = responseText + CORE_VALUE_67_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP68.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "68";
                break;
            }
        }
        if("68".equals(flag)) {
            responseText = responseText + CORE_VALUE_68_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP69.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "69";
                break;
            }
        }
        if("69".equals(flag)) {
            responseText = responseText + CORE_VALUE_69_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP70.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "70";
                break;
            }
        }
        if("70".equals(flag)) {
            responseText = responseText + CORE_VALUE_70_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP71.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "71";
                break;
            }
        }
        if("71".equals(flag)) {
            responseText = responseText + CORE_VALUE_71_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP72.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "72";
                break;
            }
        }
        if("72".equals(flag)) {
            responseText = responseText + CORE_VALUE_72_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP73.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "73";
                break;
            }
        }
        if("73".equals(flag)) {
            responseText = responseText + CORE_VALUE_73_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP74.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "74";
                break;
            }
        }
        if("74".equals(flag)) {
            responseText = responseText + CORE_VALUE_74_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP75.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "75";
                break;
            }
        }
        if("75".equals(flag)) {
            responseText = responseText + CORE_VALUE_75_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP76.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "76";
                break;
            }
        }
        if("76".equals(flag)) {
            responseText = responseText + CORE_VALUE_76_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP77.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "77";
                break;
            }
        }
        if("77".equals(flag)) {
            responseText = responseText + CORE_VALUE_77_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP78.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "78";
                break;
            }
        }
        if("78".equals(flag)) {
            responseText = responseText + CORE_VALUE_78_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP79.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "79";
                break;
            }
        }
        if("79".equals(flag)) {
            responseText = responseText + CORE_VALUE_79_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP80.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "80";
                break;
            }
        }
        if("80".equals(flag)) {
            responseText = responseText + CORE_VALUE_80_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP81.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "81";
                break;
            }
        }
        if("81".equals(flag)) {
            responseText = responseText + CORE_VALUE_81_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP82.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "82";
                break;
            }
        }
        if("82".equals(flag)) {
            responseText = responseText + CORE_VALUE_82_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP83.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "83";
                break;
            }
        }
        if("83".equals(flag)) {
            responseText = responseText + CORE_VALUE_83_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP84.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "84";
                break;
            }
        }
        if("84".equals(flag)) {
            responseText = responseText + CORE_VALUE_84_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP85.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "85";
                break;
            }
        }
        if("85".equals(flag)) {
            responseText = responseText + CORE_VALUE_85_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP86.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "86";
                break;
            }
        }
        if("86".equals(flag)) {
            responseText = responseText + CORE_VALUE_86_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP87.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "87";
                break;
            }
        }
        if("87".equals(flag)) {
            responseText = responseText + CORE_VALUE_87_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP88.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "88";
                break;
            }
        }
        if("88".equals(flag)) {
            responseText = responseText + CORE_VALUE_88_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP89.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "89";
                break;
            }
        }
        if("89".equals(flag)) {
            responseText = responseText + CORE_VALUE_89_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP90.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "90";
                break;
            }
        }
        if("90".equals(flag)) {
            responseText = responseText + CORE_VALUE_90_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP91.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "91";
                break;
            }
        }
        if("91".equals(flag)) {
            responseText = responseText + CORE_VALUE_91_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP92.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "92";
                break;
            }
        }
        if("92".equals(flag)) {
            responseText = responseText + CORE_VALUE_92_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP93.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "93";
                break;
            }
        }
        if("93".equals(flag)) {
            responseText = responseText + CORE_VALUE_93_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP94.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "94";
                break;
            }
        }
        if("94".equals(flag)) {
            responseText = responseText + CORE_VALUE_94_MESSAGE + " \n\n";
        }
        list = new ArrayList<String>(MESSAGE_MAP95.keySet());
        for(String str: list) {
            if(sendMessage.contains(str)) {
                flag = "95";
                break;
            }
        }
        if("95".equals(flag)) {
            responseText = responseText + CORE_VALUE_95_MESSAGE + " \n\n";
        }
        if(responseText!=null) {
            return responseText.substring(0, responseText.lastIndexOf("\n\n"));
        } else {
            return OTHER_MESSAGE;
        }
        /*if(MESSAGE_MAP.containsKey(sendMessage)) {
            return MESSAGE_MAP.get(sendMessage);
        } else {
            return OTHER_MESSAGE;
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
    
    static class miTM implements TrustManager, X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
        }
    }

}
