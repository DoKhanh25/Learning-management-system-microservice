import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {LazyLoadEvent, MenuItem, MessageService} from "primeng/api";
import {User} from "../../../../model/user";
import {UserService} from "../../../services/user-service/user.service";
import {Router} from "@angular/router";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrl: './account-list.component.css'
})
export class AccountListComponent implements OnInit{
  users!: User[];
  selectedUsers!: any;

  items: MenuItem[] | undefined;
  loading: boolean = true;

  first = 0;
  rows = 10;
  updateInfoForm!: FormGroup;

  searchQuery!:string;


  constructor(private userService: UserService,
              private messageService: MessageService,
              private router: Router,
              private cdr : ChangeDetectorRef
              ) {}

  ngOnInit() {

    this.userService.getAllUsers().subscribe((result) => {
      this.users = result.data;
      this.loading = false;

    }, error => {
      if(error){
        this.messageService.add(
          { severity: 'error',
            summary: 'Warn',
            detail: 'Lỗi server',
            life: 2000
          });
      }
    })

    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị tài khoản', url: '/admin/account-management' }
      ];
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickUserDetail(userId: any){
    this.router.navigate(['admin/account-detail', userId]);
  }

  clickAddAccount(){
    this.router.navigate(['admin/account-add'])
  }

  clickDeleteAccount(){
    let userIds = []
    this.loading = true;


    for (const user in this.selectedUsers) {
      userIds.push(this.selectedUsers[user].userId)
    }

    if(userIds.length == 0){
      return this.messageService.add(
        {severity: 'warn',
          summary: 'Chưa chọn',
          detail: 'Ko thành công',
          life: 2000
        });
    }

    this.userService.disableUsers(userIds).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    }, (err) => {
      this.messageService.add(
        {severity: 'error',
          summary: 'Ko thành công',
          detail: 'Ko thành công',
          life: 2000
        });
      this.resetData()
    })
  }

  resetData(){
    this.selectedUsers = null;
    this.ngOnInit()
  }

  clickDeleteCohort(){

  }

}
