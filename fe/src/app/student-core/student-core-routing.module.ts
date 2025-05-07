import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {StudentLayoutComponent} from "./student-layout/student-layout.component";
import {AuthGuard} from "../guard/auth.guard";
import {HomeComponent} from "./home/home.component";
import {CourseDetailComponent} from "./course/course-detail/course-detail.component";
import {LessonDetailComponent} from "../user-core/lesson/lesson-detail/lesson-detail.component";
import {AssignmentDetailComponent} from "./course/assignment-detail/assignment-detail.component";
import {ExamMultipleChoiceComponent} from "./quiz/exam-multiple-choice/exam-multiple-choice.component";
import {ExamEssayComponent} from "./quiz/exam-essay/exam-essay.component";
import {ExamCodingComponent} from "./quiz/exam-coding/exam-coding.component";
import {CourseListComponent} from "./course/course-list/course-list.component";

const routes: Routes = [
  {
    path: '',
    component: StudentLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'home',
        component: HomeComponent
      },
      {
        path: 'course-detail/:id',
        component: CourseDetailComponent
      },
      {
        path: 'course-list',
        component: CourseListComponent
      },
      {
        path: 'lesson-detail/:id',
        component: LessonDetailComponent
      },
      {
        path: 'assignment-detail/:id',
        component: AssignmentDetailComponent
      },
      {
        path: 'exam/:id',
        component: ExamMultipleChoiceComponent
      },
      {
        path: 'essay-exam/:id',
        component: ExamEssayComponent
      },
      {
        path: 'coding-exam/:id',
        component: ExamCodingComponent
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudentCoreRoutingModule { }
