import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';

import { StudentCoreRoutingModule } from './student-core-routing.module';
import { StudentLayoutComponent } from './student-layout/student-layout.component';
import {SharedModule} from "../shared/shared.module";
import { HomeComponent } from './home/home.component';
import {Button, ButtonDirective} from "primeng/button";
import {CardModule} from "primeng/card";
import {CarouselModule} from "primeng/carousel";
import {PrimeTemplate} from "primeng/api";
import {ProgressBarModule} from "primeng/progressbar";
import { CourseDetailComponent } from './course/course-detail/course-detail.component';
import {BreadcrumbModule} from "primeng/breadcrumb";
import {CalendarModule} from "primeng/calendar";
import {CheckboxModule} from "primeng/checkbox";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {DialogModule} from "primeng/dialog";
import {DividerModule} from "primeng/divider";
import {DropdownModule} from "primeng/dropdown";
import {EditorModule} from "primeng/editor";
import {InputTextModule} from "primeng/inputtext";
import {InputTextareaModule} from "primeng/inputtextarea";
import {PaginatorModule} from "primeng/paginator";
import {ReactiveFormsModule} from "@angular/forms";
import {Ripple} from "primeng/ripple";
import {SkeletonModule} from "primeng/skeleton";
import {TableModule} from "primeng/table";
import {TagModule} from "primeng/tag";
import {ToastModule} from "primeng/toast";
import {TooltipModule} from "primeng/tooltip";
import {ChipModule} from "primeng/chip";
import { LessonDetailComponent } from './lesson/lesson-detail/lesson-detail.component';
import { AssignmentDetailComponent } from './course/assignment-detail/assignment-detail.component';
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {TimelineModule} from "primeng/timeline";
import {MessageModule} from "primeng/message";
import {FileUploadModule} from "primeng/fileupload";
import { ExamMultipleChoiceComponent } from './quiz/exam-multiple-choice/exam-multiple-choice.component';
import { ExamEssayComponent } from './quiz/exam-essay/exam-essay.component';
import { ExamCodingComponent } from './quiz/exam-coding/exam-coding.component';
import {MonacoEditorModule} from "ngx-monaco-editor-v2";

@NgModule({
  declarations: [
    StudentLayoutComponent,
    HomeComponent,
    CourseDetailComponent,
    LessonDetailComponent,
    AssignmentDetailComponent,
    ExamMultipleChoiceComponent,
    ExamEssayComponent,
    ExamCodingComponent,
  ],
  imports: [
    CommonModule,
    StudentCoreRoutingModule,
    SharedModule,
    Button,
    CardModule,
    CarouselModule,
    PrimeTemplate,
    ProgressBarModule,
    BreadcrumbModule,
    ButtonDirective,
    CalendarModule,
    CheckboxModule,
    ConfirmDialogModule,
    DialogModule,
    DividerModule,
    DropdownModule,
    EditorModule,
    InputTextModule,
    InputTextareaModule,
    PaginatorModule,
    ReactiveFormsModule,
    Ripple,
    SkeletonModule,
    TableModule,
    TagModule,
    ToastModule,
    TooltipModule,
    ChipModule,
    ProgressSpinnerModule,
    TimelineModule,
    MessageModule,
    FileUploadModule,
    MonacoEditorModule
  ],
  schemas: [
    CUSTOM_ELEMENTS_SCHEMA
  ]
})
export class StudentCoreModule { }
