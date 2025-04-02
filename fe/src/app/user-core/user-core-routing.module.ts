import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {UserLayoutComponent} from "./user-layout/user-layout.component";
import {AuthGuard} from "../guard/auth.guard";
import {HomeComponent} from "./home/home/home.component";
import {CourseDetailComponent} from "./course/course-detail/course-detail.component";
import {LessonDetailComponent} from "./lesson/lesson-detail/lesson-detail.component";
import {LessonManagementComponent} from "./lesson/lesson-management/lesson-management.component";
import {RoleGuard} from "../guard/role/role.guard";
import {CourseManagementComponent} from "./course/course-management/course-management.component";
import {AssignmentManagementComponent} from "./course/assignment-management/assignment-management.component";
import {LessonStudentManagementComponent} from "./lesson/lesson-student-management/lesson-student-management.component";
import {CourseListComponent} from "./course/course-list/course-list.component";
import { QuestionBankManagementComponent } from './quiz/question/question-bank-management/question-bank-management.component';
import { QuestionBankDetailComponent } from './quiz/question/question-bank-detail/question-bank-detail.component';
import { ExamManagementComponent } from './quiz/exam/exam-management/exam-management.component';
import { ExamDetailComponent } from './quiz/exam/exam-detail/exam-detail.component';

const routes: Routes = [
  {
    path: '',
    component: UserLayoutComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ["ROLE_ADMIN", "ROLE_TEACHER"]
    },
    children: [
      {
        path: 'home',
        component: HomeComponent
      },
      {
        path: 'course-list',
        component: CourseListComponent
      },
      {
        path: 'course-detail/:id',
        component: CourseDetailComponent
      },
      {
        path: 'course-management/:id',
        component: CourseManagementComponent
      },
      {
        path: 'lesson-detail/:id',
        component: LessonDetailComponent
      },
      {
        path: 'lesson-management/:id',
        component: LessonManagementComponent
      },
      {
        path: 'assignment-management/:id',
        component:  AssignmentManagementComponent
      },
      {
        path: 'lesson-student-management/:id',
        component: LessonStudentManagementComponent
      },
      {
        path: 'course/:courseId/question-bank-management',
        component: QuestionBankManagementComponent
      },
      {
        path: 'course/:courseId/question-bank-detail/:id',
        component: QuestionBankDetailComponent
      },
      {
        path: 'course/:courseId/exam-management',
        component: ExamManagementComponent
      },
      {
        path: 'course/:courseId/exam-detail/:examId',
        component: ExamDetailComponent
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
export class UserCoreRoutingModule { }
