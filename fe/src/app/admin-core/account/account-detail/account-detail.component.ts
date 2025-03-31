import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {ActivatedRoute} from "@angular/router";
import {UserService} from "../../../services/user-service/user.service";
import {Attribute, User, UserSession} from "../../../../model/user";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.css'
})
export class AccountDetailComponent implements OnInit{
  user!: User;
  updateUser!: User;
  updateInfoForm!: FormGroup

  userSessions!: UserSession[]
  newestSession!: UserSession;
  visible: boolean = false;

  stateOptions: any[] = [
    { label: 'Dừng hoạt đông', value: false },
    { label: 'Hoạt động', value: true }
  ];


  items: MenuItem[] | undefined;
  userId!: any;

  loading: boolean = true;
  sessionLoading: boolean = true;

  constructor(private route: ActivatedRoute,
              private userService: UserService,
              private messageService: MessageService,
              private fb: FormBuilder) {
    this.userId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị tài khoản', url: '/admin/account-management' },
        {label: 'Thông tin tài khoản'}
      ];


    this.updateInfoForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      enable: [false, Validators.required],
      attributes: this.fb.group({
        phone: [''],
        address: [''],
        country: [''],
        city: [''],
        department: [''],
        institution: ['']
      })
    })

    this.userService.getUserById(this.userId).subscribe((rs) => {
        this.user = rs;
        this.loading = false;
        this.updateFormControlValue(this.user)

      },
      error => {
        console.log(error)
      })

    this.userService.getUserSessionById(this.userId).subscribe((rs) => {
      this.userSessions = rs;
      this.sessionLoading = false;
    } , (err) => {
      console.log(err)
    })

    if(this.userSessions != null && this.userSessions.length > 0){
      this.newestSession = this.userSessions[0];
    }
  }

  openUpdateDialog(){
    this.visible = true;
  }

  updateFormControlValue(updateUser: User){
    this.updateInfoForm.setValue({
      email: updateUser.email,
      lastName: updateUser.lastName,
      firstName: updateUser.firstName,
      enable: updateUser.enable,
      attributes: {
        phone: updateUser.attributes?.phone?.[0],
        country: updateUser.attributes?.country?.[0],
        address: updateUser.attributes?.address?.[0],
        city: updateUser.attributes?.city?.[0],
        department: updateUser.attributes?.department?.[0],
        institution: updateUser.attributes?.institution?.[0]
      }
    })
  }

  clickUpdateUser(){
    let attributes: Attribute = {
      phone: [this.updateInfoForm.get('attributes.phone')?.value],
      address: [this.updateInfoForm.get('attributes.address')?.value],
      city: [this.updateInfoForm.get('attributes.city')?.value],
      country: [this.updateInfoForm.get('attributes.country')?.value],
      department: [this.updateInfoForm.get('attributes.department')?.value],
      institution: [this.updateInfoForm.get('attributes.address')?.value]
    }
    this.updateUser = {
      username: this.updateInfoForm.get('username')?.value,
      lastName: this.updateInfoForm.get('lastName')?.value,
      firstName: this.updateInfoForm.get('firstName')?.value,
      enable: this.updateInfoForm.get('enable')?.value,
      email: this.updateInfoForm.get('email')?.value,
      attributes: attributes
    }
    this.visible = false;


    this.userService.updateUser(this.updateUser, this.userId).subscribe((result) => {
      if(result.status == 1){
         this.messageService.add(
          {severity: 'success',
            summary: 'Thành công',
            detail: 'Cập nhật tài khoản thành công',
            life: 2000
          });
         this.ngOnInit()
      }
      else {
        this.messageService.add(
          {severity: 'warn',
            summary: 'Không thành công',
            detail: 'Cập nhật tài khoản không thành công',
            life: 2000
          });
      }
    }, (error) => {
       this.messageService.add(
        {severity: 'error',
          summary: 'Lỗi server',
          detail: 'Lỗi server',
          life: 2000
        });
    })
  }


}
