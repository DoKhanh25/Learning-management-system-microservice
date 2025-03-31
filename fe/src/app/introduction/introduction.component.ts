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
    if(this.keycloakService.isLoggedIn() && this.keycloakService.getUserRoles().indexOf("ROLE_ADMIN") > -1){
      console.log("ok")
      await this.router.navigate(["/admin/home"])
    }
  }

  navigateToLogin(): void{
    this.keycloakService.login({
      redirectUri: window.location.origin + "/admin/home"
    })
  }
  navigateToRegister(): void{
    this.router.navigate(['/auth/register']);
  }
}
