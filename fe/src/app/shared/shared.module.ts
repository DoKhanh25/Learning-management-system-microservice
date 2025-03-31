import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MegaMenuModule } from 'primeng/megamenu';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { AvatarGroupModule } from 'primeng/avatargroup';
import { MenubarModule } from 'primeng/menubar';
import { MenuModule } from 'primeng/menu';
import { SidebarModule } from 'primeng/sidebar';
import { RippleModule } from 'primeng/ripple';
import { StyleClassModule } from 'primeng/styleclass';
import { VideoPlayerComponent } from './video-player/video-player.component';
import {SafePipe} from "../pipe/safe.pipe";
import { DocumentViewerComponent } from './document-viewer/document-viewer.component';
import {ProgressSpinnerModule} from "primeng/progressspinner";
import { MultipleVideoPlayerComponent } from './multiple-video-player/multiple-video-player.component';
import {VgOverlayPlayModule} from "@videogular/ngx-videogular/overlay-play";
import {VgCoreModule} from "@videogular/ngx-videogular/core";
import {VgControlsModule} from "@videogular/ngx-videogular/controls";
import {VgBufferingModule} from "@videogular/ngx-videogular/buffering";
import {PdfViewerModule} from "ng2-pdf-viewer";
import { AiChatAssistantComponent } from './ai-chat-assistant/ai-chat-assistant.component';
import { FormsModule } from '@angular/forms';
import {InputTextModule} from "primeng/inputtext";
import {BoldMarkdownPipe} from "../pipe/boldMarkdown/bold-markdown.pipe";
import { ChatComponent } from './chat/chat.component';
import {TabViewModule} from "primeng/tabview";
import {MultiSelectModule} from "primeng/multiselect";
import {DialogModule} from "primeng/dialog";
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';


@NgModule({
  declarations: [
    HeaderComponent,
    VideoPlayerComponent,
    DocumentViewerComponent,
    MultipleVideoPlayerComponent,
    AiChatAssistantComponent,
    ChatComponent,
    UnauthorizedComponent,
  ],
  imports: [
    CommonModule,
    MegaMenuModule,
    ButtonModule,
    AvatarModule,
    AvatarGroupModule,
    MenubarModule,
    MenuModule,
    SidebarModule,
    RippleModule,
    StyleClassModule,
    SafePipe,
    ProgressSpinnerModule,
    VgOverlayPlayModule,
    VgCoreModule,
    VgControlsModule,
    VgBufferingModule,
    PdfViewerModule,
    FormsModule,
    InputTextModule,
    BoldMarkdownPipe,
    TabViewModule,
    MultiSelectModule,
    DialogModule
  ]
  ,
  exports: [
    HeaderComponent,
    VideoPlayerComponent,
    MultipleVideoPlayerComponent,
    DocumentViewerComponent,
    AiChatAssistantComponent,
    ChatComponent
  ]
})
export class SharedModule { }
