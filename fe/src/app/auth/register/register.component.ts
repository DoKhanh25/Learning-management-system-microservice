import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  title = 'Đăng ký';
  value = '';
  image = 'assets/introduction/research.jpg';
  registerForm: any;
  isSubmitted = false;

  constructor() {}

  ngOnInit(): void {
    this.registerForm = new FormGroup({
      customerName: new FormControl('', Validators.required),
      username: new FormControl('', Validators.required ),
      password: new FormControl('', 
        [Validators.required, 
        Validators.minLength(8), 
        Validators.pattern(/^(?=\D*\d)(?=[^a-z]*[a-z])(?=[^A-Z]*[A-Z]).{8,30}$/)] )
    });
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.registerForm.invalid) {
      return;
    }
    
  }
}
