import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IntroductionComponent } from './introduction/introduction.component';
import {UnauthorizedComponent} from "./shared/unauthorized/unauthorized.component";

const routes: Routes = [
  { path: '', redirectTo: 'introduce', pathMatch: 'full' },
  {
    path: 'introduce', component: IntroductionComponent
  },
  {
    path: 'admin',
    loadChildren: () => import('./admin-core/admin-core.module').then(m => m.AdminCoreModule)
  },
  {
    path: 'user',
    loadChildren: () => import('./user-core/user-core.module').then(m => m.UserCoreModule),
  },
  {
    path: 'student',
    loadChildren: () => import('./student-core/student-core.module').then(m => m.StudentCoreModule),
  },
  {
    path: 'unauthorized',
    component: UnauthorizedComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
