/*
 * Copyright 2018 LINE Corporation
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

import static java.util.Arrays.asList;

import java.net.URI;
import java.util.function.Supplier;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.Button.ButtonHeight;
import com.linecorp.bot.model.message.flex.component.Button.ButtonStyle;
import com.linecorp.bot.model.message.flex.component.Icon;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectMode;
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectRatio;
import com.linecorp.bot.model.message.flex.component.Image.ImageSize;
import com.linecorp.bot.model.message.flex.component.Separator;
import com.linecorp.bot.model.message.flex.component.Spacer;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.component.Text.TextWeight;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;

public class CustomizedFlexMessageSupplier implements Supplier<FlexMessage> {
    @Override
    public FlexMessage get() {
        final Box headerBlock = createHeaderBlock();
        final Box bodyBlock = createBodyBlock();
        final Bubble bubble =
                Bubble.builder()
                      .header(headerBlock)
                      .body(bodyBlock)
                      .build();

        return new FlexMessage("智能客服", bubble);
    }
    
    private Box createHeaderBlock() {
        final Text title =
                Text.builder()
                    .text("您好，我是智能客服小樂。很高興為您服務，提供常用功能快速連結給您：")
                    .weight(TextWeight.BOLD)
                    .size(FlexFontSize.LG)
                    .build();

        return Box.builder()
                  .layout(FlexLayout.VERTICAL)
                  .spacing(FlexMarginSize.SM)
                  .contents(asList(title))
                  .build();
    }

    private Box createBodyBlock() {
        final Box box = createBox();

        final Box box2 = createBox2();

        return Box.builder()
                  .layout(FlexLayout.VERTICAL)
                  .contents(asList(box, box2))
                  .build();
    }

    private Box createBox() {
        final Image image1 =
                Image.builder()
                     .url(createUri("/static/icon/1.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("查詢進度", "status"))
                     .build();
        final Image image2 =
                Image.builder()
                     .url(createUri("/static/icon/2.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("文件上傳", "reupload"))
                     .build();
        final Image image3 =
                Image.builder()
                     .url(createUri("/static/icon/3.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("立即開卡", "activate"))
                     .build();

        return Box.builder()
                  .layout(FlexLayout.HORIZONTAL)
                  .contents(asList(image1, image2, image3))
                  .build();
    }

    private Box createBox2() {
        final Image image4 =
                Image.builder()
                     .url(createUri("/static/icon/4.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("最新活動", "campaign"))
                     .build();
        final Image image5 =
                Image.builder()
                     .url(createUri("/static/icon/5.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("24小時卡友專線", "faq"))
                     .build();
        final Image image6 =
                Image.builder()
                     .url(createUri("/static/icon/6.jpg"))
                     .aspectMode(ImageAspectMode.Cover)
                     .action(new MessageAction("行動APP", "app"))
                     .build();

        return Box.builder()
                  .layout(FlexLayout.HORIZONTAL)
                  .contents(asList(image4, image5, image6))
                  .build();
    }

    private static URI createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .scheme("https")
                                          .path(path).build()
                                          .toUri();
    }
}
