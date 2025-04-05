import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import {KeycloakService} from "keycloak-angular";

@Component({
  selector: 'app-introduction',
  templateUrl: './introduction.component.html',
  styleUrl: './introduction.component.css'
})
export class IntroductionComponent implements OnInit{
  title = 'Giới thiệu';
  image = 'assets/introduction/intro-bg.gif';

  constructor(private router: Router, private keycloakService: KeycloakService) {

  }
  async ngOnInit() {
    console.log(this.keycloakService.getUserRoles())
    if(this.keycloakService.isLoggedIn() ){
      if(this.keycloakService.getUserRoles().indexOf("ROLE_ADMIN") > -1){
        await this.router.navigate(["/admin/home"])
      } else if(this.keycloakService.getUserRoles().indexOf("ROLE_TEACHER") > -1 && this.keycloakService.getUserRoles().indexOf("ROLE_ADMIN") < 0){
        await this.router.navigate(["/user/home"])
      } else {
        await this.router.navigate(["/student/home"])
      }
    }
  }

  navigateToLogin(): void {
    this.keycloakService.login({
      redirectUri: window.location.origin
    }).then(() => {
      if (this.keycloakService.isLoggedIn()) {
        const roles = this.keycloakService.getUserRoles();
        if (roles.includes("ROLE_ADMIN")) {
          this.router.navigate(["/admin/home"]);
        } else if (roles.includes("ROLE_TEACHER") && !roles.includes("ROLE_ADMIN")) {
          this.router.navigate(["/user/home"]);
        } else {
          this.router.navigate(["/student/home"]);
        }
      }
    });
  }

}
