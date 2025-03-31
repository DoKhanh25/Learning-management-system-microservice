import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminCoreRoutingModule } from './admin-core-routing.module';
import { SharedModule } from '../shared/shared.module';
import { ExaminationManagementComponent } from './examination/examination-management/examination-management.component';
import { ExaminationListComponent } from './examination/examination-list/examination-list.component';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { DataViewModule } from 'primeng/dataview';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { TagModule } from 'primeng/tag';
import { RatingModule } from 'primeng/rating';
import { ButtonModule } from 'primeng/button';
import {  CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { AccountListComponent } from './account/account-list/account-list.component';
import {TableModule} from "primeng/table";
import {IconFieldModule} from "primeng/iconfield";
import {InputTextModule} from "primeng/inputtext";
import {InputIconModule} from "primeng/inputicon";
import {ButtonGroupModule} from "primeng/buttongroup";
import {TooltipModule} from "primeng/tooltip";
import {ProgressBarModule} from "primeng/progressbar";
import { AccountDetailComponent } from './account/account-detail/account-detail.component';
import {ConfirmationService, MessageService} from "primeng/api";
import { ToastModule} from "primeng/toast";
import {AvatarModule} from "primeng/avatar";
import {MessageModule} from "primeng/message";
import {CardModule} from "primeng/card";
import {DialogModule} from "primeng/dialog";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserModule} from "@angular/platform-browser";
import {SelectButtonModule} from "primeng/selectbutton";
import { AccountAddComponent } from './account/account-add/account-add.component';
import {
  NgbCollapseModule,
  NgbNav,
  NgbNavContent,
  NgbNavItem,
  NgbNavLinkBase, NgbNavModule,
  NgbNavOutlet
} from "@ng-bootstrap/ng-bootstrap";
import {FileUploadModule} from "primeng/fileupload";
import {BadgeModule} from "primeng/badge";
import { RoleListComponent } from './role/role-list/role-list.component';
import { RoleDetailComponent } from './role/role-detail/role-detail.component';
import {TabMenuModule} from "primeng/tabmenu";
import { ResourceListComponent } from './resource/resource-list/resource-list.component';
import { PolicyListComponent } from './policy/policy-list/policy-list.component';
import {Ripple} from "primeng/ripple";
import { PolicyDetailComponent } from './policy/policy-detail/policy-detail.component';
import {RadioButtonModule} from "primeng/radiobutton";
import {CheckboxModule} from "primeng/checkbox";
import { PolicyAddComponent } from './policy/policy-add/policy-add.component';
import { PermissionListComponent } from './permission/permission-list/permission-list.component';
import { PermissionAddComponent } from './permission/permission-add/permission-add.component';
import {MultiSelectModule} from "primeng/multiselect";
import { PermissionDetailComponent } from './permission/permission-detail/permission-detail.component';
import { CohortListComponent } from './cohort/cohort-list/cohort-list.component';
import { CohortAddComponent } from './cohort/cohort-add/cohort-add.component';
import {PickListModule} from "primeng/picklist";
import { CohortDetailComponent } from './cohort/cohort-detail/cohort-detail.component';
import { CourseAddComponent } from './course/course-add/course-add.component';
import { CourseListComponent } from './course/course-list/course-list.component';
import {CalendarModule} from "primeng/calendar";
import {TabViewModule} from "primeng/tabview";
import {AccordionModule} from "primeng/accordion";
import { CourseDetailComponent } from './course/course-detail/course-detail.component';
import {InputTextareaModule} from "primeng/inputtextarea";
import { DropdownModule } from 'primeng/dropdown';
import {ConfirmDialogModule} from "primeng/confirmdialog";
import { HomeComponent } from './home/home/home.component';
import {TimelineModule} from "primeng/timeline";
import {RouterModule} from "@angular/router";

@NgModule({
  declarations: [
    ExaminationManagementComponent,
    ExaminationListComponent,
    AdminLayoutComponent,
    AccountListComponent,
    AccountDetailComponent,
    AccountAddComponent,
    RoleListComponent,
    RoleDetailComponent,
    ResourceListComponent,
    PolicyListComponent,
    PolicyDetailComponent,
    PolicyAddComponent,
    PermissionListComponent,
    PermissionAddComponent,
    PermissionDetailComponent,
    CohortListComponent,
    CohortAddComponent,
    CohortDetailComponent,
    CourseAddComponent,
    CourseListComponent,
    CourseDetailComponent,
    HomeComponent
  ],
  imports: [
    CommonModule,
    AdminCoreRoutingModule,
    SharedModule,
    DataViewModule,
    BreadcrumbModule,
    TagModule,
    RatingModule,
    ButtonModule,
    TableModule,
    IconFieldModule,
    InputTextModule,
    InputIconModule,
    ButtonGroupModule,
    TooltipModule,
    ProgressBarModule,
    ToastModule,
    TagModule,
    AvatarModule,
    MessageModule,
    CardModule,
    DialogModule,
    FormsModule,
    ReactiveFormsModule,
    SelectButtonModule,
    NgbCollapseModule,
    FileUploadModule,
    BadgeModule,
    TabMenuModule,
    NgbNavModule,
    Ripple,
    RadioButtonModule,
    CheckboxModule,
    MultiSelectModule,
    PickListModule,
    CalendarModule,
    TabViewModule,
    AccordionModule,
    InputTextareaModule,
    DropdownModule,
    ConfirmDialogModule,
    TimelineModule,
    RouterModule
  ],
  providers: [
    ConfirmationService
  ]
})
export class AdminCoreModule { }
