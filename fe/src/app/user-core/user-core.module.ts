import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserCoreRoutingModule } from './user-core-routing.module';
import { UserLayoutComponent } from './user-layout/user-layout.component';
import {SharedModule} from "../shared/shared.module";
import { HomeComponent } from './home/home/home.component';
import {BreadcrumbModule} from "primeng/breadcrumb";
import {PrimeTemplate} from "primeng/api";
import {CarouselModule} from "primeng/carousel";
import {CardModule} from "primeng/card";
import {FileUploadModule} from "primeng/fileupload";
import {TableModule} from "primeng/table";
import {DropdownModule} from "primeng/dropdown";
import { CourseListComponent } from './course/course-list/course-list.component';
import { CourseDetailComponent } from './course/course-detail/course-detail.component';
import {DialogModule} from "primeng/dialog";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DividerModule} from "primeng/divider";
import {SkeletonModule} from "primeng/skeleton";
import {InputTextModule} from "primeng/inputtext";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ToastModule} from "primeng/toast";
import {Ripple} from "primeng/ripple";
import { LessonDetailComponent } from './lesson/lesson-detail/lesson-detail.component';
import {EditorModule} from "primeng/editor";
import { LessonManagementComponent } from './lesson/lesson-management/lesson-management.component';
import {CdkDrag, CdkDragHandle, CdkDropList} from "@angular/cdk/drag-drop";
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {TagModule} from "primeng/tag";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {SelectButtonModule} from "primeng/selectbutton";
import {SafePipe} from "../pipe/safe.pipe";
import {CheckboxModule} from "primeng/checkbox";
import {CalendarModule} from "primeng/calendar";
import { CourseManagementComponent } from './course/course-management/course-management.component';
import {TabViewModule} from "primeng/tabview";
import {AccordionModule} from "primeng/accordion";
import { LessonStudentManagementComponent } from './lesson/lesson-student-management/lesson-student-management.component';
import { AssignmentManagementComponent } from './course/assignment-management/assignment-management.component';
import {InputNumberModule} from "primeng/inputnumber";
import {BadgeModule} from "primeng/badge";
import { QuestionBankManagementComponent } from './quiz/question/question-bank-management/question-bank-management.component';
import { QuestionBankDetailComponent } from './quiz/question/question-bank-detail/question-bank-detail.component';
import { ExamManagementComponent } from './quiz/exam/exam-management/exam-management.component';
import { ExamDetailComponent } from './quiz/exam/exam-detail/exam-detail.component';
import {MultiSelectModule} from "primeng/multiselect";
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { ExamResultManagementComponent } from './quiz/exam/exam-result-management/exam-result-management.component';
import {BlockUIModule} from "primeng/blockui";
import { CourseStatisticComponent } from './course/course-statistic/course-statistic.component';
import {TreeModule} from "primeng/tree";
import {ChipModule} from "primeng/chip";


@NgModule({
  declarations: [
    UserLayoutComponent,
    HomeComponent,
    CourseListComponent,
    CourseDetailComponent,
    LessonDetailComponent,
    LessonManagementComponent,
    CourseManagementComponent,
    LessonStudentManagementComponent,
    AssignmentManagementComponent,
    QuestionBankManagementComponent,
    QuestionBankDetailComponent,
    ExamManagementComponent,
    ExamDetailComponent,
    ExamResultManagementComponent,
    CourseStatisticComponent
  ],
    imports: [
        SafePipe,
        CommonModule,
        UserCoreRoutingModule,
        SharedModule,
        BreadcrumbModule,
        PrimeTemplate,
        CarouselModule,
        CardModule,
        FileUploadModule,
        TableModule,
        DropdownModule,
        DialogModule,
        ReactiveFormsModule,
        DividerModule,
        SkeletonModule,
        InputTextModule,
        InputTextareaModule,
        ToastModule,
        Ripple,
        EditorModule,
        FormsModule,
        CdkDropList,
        ProgressSpinnerModule,
        TagModule,
        ConfirmDialogModule,
        CdkDragHandle,
        CdkDrag,
        SelectButtonModule,
        CheckboxModule,
        CalendarModule,
        TabViewModule,
        AccordionModule,
        InputNumberModule,
        BadgeModule,
        MultiSelectModule,
        MonacoEditorModule,
        BlockUIModule,
        TreeModule,
        ChipModule
    ],
  schemas: [
    CUSTOM_ELEMENTS_SCHEMA
  ]
})
export class UserCoreModule { }
