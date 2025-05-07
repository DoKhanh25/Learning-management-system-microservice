import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {Role} from "../../../../model/role";
import {RoleService} from "../../../services/role/role.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {User} from "../../../../model/user";
import {UserService} from "../../../services/user-service/user.service";

@Component({
  selector: 'app-role-detail',
  templateUrl: './role-detail.component.html',
  styleUrl: './role-detail.component.css'
})
export class RoleDetailComponent implements OnInit, OnChanges{
  items: MenuItem[] | undefined;
  active = 1;
  updateRoleForm!: FormGroup

  roleName!: any;
  first = 0;
  rows = 10;
  roles!: Role[]
  loading = true;
  selectedRoles!: any;

  searchQuery!:string;
  searchQuery2!:string;
  loading2 = true;
  searchQuery3!:string;
  searchQuery4!:string;

  visible = false

  addUserDialogVisible = false;

  users!: any[]

  selectedAssignRole!: any;

  allRoles!: Role[]

  allUsers!: User[]

  selectedUsers!: any;

  selectedUserInRole!: any;




  constructor(private roleService: RoleService,
              private messageService: MessageService,
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder) {
    this.roleName = this.route.snapshot.paramMap.get('roleName');


  }
  ngOnInit() {
    this.items =
      [
        {icon: 'pi pi-home', route: '/'},
        {label: 'Quản lý vai trò', url: '/admin/role-management'},
        {label: 'Thông tin vai trò'}
      ];

    this.updateRoleForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
    })
    this.updateRoleForm.get('name')?.disable();

    this.roleService.getRoleByName(this.roleName).subscribe((result) => {
      this.updateRoleForm.setValue({
        name: result.data.name,
        description: result.data.description
      })
    }, (err) => {
      console.log(err)
    })


    this.roleService.getCompositeRoles(this.roleName).subscribe((result) => {
      this.roles = result.data;
      this.loading = false;


    }, (err) => {
      console.log(err)
      this.loading = false;

    })

    this.roleService.getUsersInRole(this.roleName).subscribe((result) => {
      this.users = result.data;
      this.loading2 = false;


    }, (err) => {
      console.log(err)
      this.loading2 = false;
    })


    this.roleService.getAllRoles().subscribe((result) => {
      this.allRoles = result.data;
    }
      , (err) => {
        console.log(err)
      })

    this.userService.getAllUsers().subscribe((result) => {
      this.allUsers = result.data
      console.log(this.allUsers)
    })
  }

  ngOnChanges(changes: SimpleChanges) {

  }

  clickUpdateRole(){
    let data = {
      name: this.updateRoleForm.get('name')?.value,
      description: this.updateRoleForm.get('description')?.value
    }
    this.roleService.updateRoleDetail(data).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    })

  }
  clickAddUsersInRole(){
    let userIds = []
    for (const user in this.selectedUsers) {
      userIds.push(this.selectedUsers[user].userId);
    }

    this.roleService.updateUsersInRole(this.roleName, userIds).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    }, (error) => {
      this.messageService.add(
        {severity: 'warn',
          summary: 'warn',
          detail: error.detail,
          life: 2000
        });
      this.resetData()
    })
  }

  clickRemoveUsersInRole(){
    let userIds = []

    for (const user in this.selectedUserInRole) {
      userIds.push(this.selectedUserInRole[user].id);
    }
    console.log(userIds)

    this.roleService.removeUsersInRole(this.roleName, userIds).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    }, (error) => {
      this.messageService.add(
        {severity: 'warn',
          summary: 'warn',
          detail: error.detail,
          life: 2000
        });
      this.resetData()
    })

  }


  clickAssignRole(){
    let roleNames = []
    this.loading = true;

    for (const role in this.selectedAssignRole) {
      roleNames.push(this.selectedAssignRole[role].name);
    }

    this.roleService.addComposites(this.roleName, roleNames).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    })
  }

  clickUnassignRole(){
    let roleNames = []
    this.loading = true;

    for (const role in this.selectedRoles) {
      roleNames.push(this.selectedRoles[role].name);
    }

    this.roleService.unsignedComposites(this.roleName, roleNames).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    })
  }

  resetData(){
    this.selectedRoles = null;
    this.selectedAssignRole = null;
    this.visible = false;
    this.selectedUsers = null;
    this.addUserDialogVisible = false;
    this.ngOnInit();

  }



  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }
}
