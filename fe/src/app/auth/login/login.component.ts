import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OnInit } from '@angular/core';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  title = 'Đăng nhập';
  value = '';
  image = 'assets/introduction/research.jpg';
  loginForm: any;
  isSubmitted = false;

  constructor() {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      username: new FormControl('', Validators.required ),
      password: new FormControl('', 
        [Validators.required, 
        Validators.minLength(8), 
        Validators.pattern(/^(?=\D*\d)(?=[^a-z]*[a-z])(?=[^A-Z]*[A-Z]).{8,30}$/)] )
    });
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.loginForm.invalid) {
      return;
    }
    
  }

  
  
 
  
}
