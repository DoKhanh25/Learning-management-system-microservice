import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExaminationManagementComponent } from './examination/examination-management/examination-management.component';
import { ExaminationListComponent } from './examination/examination-list/examination-list.component';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import {AccountListComponent} from "./account/account-list/account-list.component";
import {AccountDetailComponent} from "./account/account-detail/account-detail.component";
import {AccountAddComponent} from "./account/account-add/account-add.component";
import {RoleListComponent} from "./role/role-list/role-list.component";
import {RoleDetailComponent} from "./role/role-detail/role-detail.component";
import {ResourceListComponent} from "./resource/resource-list/resource-list.component";
import {PolicyListComponent} from "./policy/policy-list/policy-list.component";
import {PolicyDetailComponent} from "./policy/policy-detail/policy-detail.component";
import {PolicyAddComponent} from "./policy/policy-add/policy-add.component";
import {PermissionListComponent} from "./permission/permission-list/permission-list.component";
import {PermissionAddComponent} from "./permission/permission-add/permission-add.component";
import {PermissionDetailComponent} from "./permission/permission-detail/permission-detail.component";
import {CohortListComponent} from "./cohort/cohort-list/cohort-list.component";
import {CohortAddComponent} from "./cohort/cohort-add/cohort-add.component";
import {CohortDetailComponent} from "./cohort/cohort-detail/cohort-detail.component";
import {CourseListComponent} from "./course/course-list/course-list.component";
import {CourseAddComponent} from "./course/course-add/course-add.component";
import {CourseDetailComponent} from "./course/course-detail/course-detail.component";
import {AuthGuard} from "../guard/auth.guard";
import {HomeComponent} from "./home/home/home.component";
import {RoleGuard} from "../guard/role/role.guard";

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['ROLE_ADMIN']
    },
    children: [
      {
        path: 'home',
        component: HomeComponent
      },
      {
        path: 'account-management',
        component: AccountListComponent
      },
      {
        path: 'account-detail/:id',
        component: AccountDetailComponent
      },
      {
        path: 'account-add',
        component: AccountAddComponent
      },
      {
        path: 'exam-management',
        component: ExaminationManagementComponent
      },
      {
        path: 'role-management',
        component: RoleListComponent
      },
      {
        path: 'role-detail/:roleName',
        component: RoleDetailComponent
      },
      {
        path: 'resource-management',
        component: ResourceListComponent
      },
      {
        path: 'exam-list',
        component: ExaminationListComponent
      },
      {
        path: 'policy-management',
        component: PolicyListComponent
      },
      {
        path: 'policy-detail/:id',
        component: PolicyDetailComponent
      },
      {
        path: 'policy-add',
        component: PolicyAddComponent
      },
      {
        path: 'permission-management',
        component: PermissionListComponent
      },
      {
        path: 'permission-add',
        component: PermissionAddComponent
      },
      {
        path: 'permission-detail/:id',
        component: PermissionDetailComponent
      },
      {
        path: "cohort-management",
        component: CohortListComponent
      },
      {
        path: "cohort-add",
        component: CohortAddComponent
      },
      {
        path: "cohort-detail/:id",
        component: CohortDetailComponent
      },
      {
        path: "course-management",
        component: CourseListComponent
      },
      {
        path: "course-add",
        component: CourseAddComponent
      },
      {
        path: "course-detail/:id",
        component: CourseDetailComponent
      },
      {
        path: '', // Route mặc định
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
export class AdminCoreRoutingModule { }
