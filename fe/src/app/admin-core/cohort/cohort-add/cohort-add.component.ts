import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user-service/user.service";
import {User} from "../../../../model/user";
import {Cohort} from "../../../../model/cohort";
import {CohortService} from "../../../services/cohort/cohort.service";

@Component({
  selector: 'app-cohort-add',
  templateUrl: './cohort-add.component.html',
  styleUrl: './cohort-add.component.css'
})
export class CohortAddComponent implements OnInit{
  items: MenuItem[] | undefined;
  cohortAddForm!: FormGroup;
  sourceUsers!: User[];
  targetUsers!: User[];

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private messageService: MessageService,
              private cdr: ChangeDetectorRef,
              private cohortService: CohortService) {
  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị nhóm', url: '/admin/cohort-management'},
        {label: 'Thêm nhóm'}
      ];

    this.targetUsers = [];

    this.cohortAddForm = this.fb.group({
      name: ["", Validators.required],
      description: [""]
    })

    this.userService.getAllUsers().subscribe((result) => {
      if(result.status == 1){
        this.sourceUsers = result.data;
      }
    }, (err) => {
      this.messageService.add(
        { severity: 'error',
          summary: 'Warn',
          detail: 'Lỗi server',
          life: 2000
        });
    })
  }

  clickAddCohort(){
    let cohort = {
      name: this.cohortAddForm.get("name")?.value,
      description: this.cohortAddForm.get("description")?.value,
      userIds: this.getUserIds(this.targetUsers)
    }

    this.cohortService.addCohort(cohort).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Tạo thành công',
          life: 2000
        })
      }
    }, (err) => {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thành công',
        life: 2000
      })
    })


  }

  getUserIds(users: User[]){
    if(users.length == 0){
      return null;
    }
    let userIds = [];
    for (let i = 0; i < users.length; i++) {
      userIds.push(users[i].userId);
    }
    return userIds;
  }

}
