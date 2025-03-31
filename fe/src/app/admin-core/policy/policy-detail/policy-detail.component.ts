import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RadioButton} from "primeng/radiobutton";
import {Policy, Role, RolePolicy} from "../../../../model/role";
import {RoleService} from "../../../services/role/role.service";
import {ActivatedRoute, Route, Router} from "@angular/router";

@Component({
  selector: 'app-policy-detail',
  templateUrl: './policy-detail.component.html',
  styleUrl: './policy-detail.component.css'
})
export class PolicyDetailComponent implements OnInit{
  items: MenuItem[] | undefined;
  policyDetailForm!: FormGroup
  selectedLogic:string = "POSITIVE";

  visible = false;
  allRoles!: Role[];
  selectedRoles: Role[] = [];
  selectedAssignRole!: Role[];


  first = 0;
  rows = 10;

  searchQuery!:string;

  policyId!: any;


  policyPost!: Policy;

  constructor(private fb: FormBuilder,
              private roleService: RoleService,
              private route: ActivatedRoute,
              private messageService: MessageService) {
    this.policyId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý chính sách', url: '/admin/policy-management' },
        { label: 'Thông tin chính sách' }
      ];

    this.policyDetailForm = this.fb.group({
      name: ["", [Validators.required]],
      description: [""]
    })

    this.policyDetailForm.get('name')?.disable();

    this.roleService.getAllRoles().subscribe((rs) => {
      this.allRoles = rs.data
    })

    this.roleService.getClientPolicyById(this.policyId).subscribe((result) => {

      this.policyDetailForm.setValue({
        name: result.data.name,
        description: result.data.description
      })

      this.policyPost = {
        id: result.data.id,
        name: result.data.name,
        description: result.data.description,
        config: result.data.config
      }

      this.selectedLogic = result.data.logic


      if(result.data.config != null &&
        result.data.config.roleDetails != null &&
        result.data.config.roles != null){

        const roleDetails = JSON.parse(result.data.config.roleDetails);
        this.selectedRoles = roleDetails;
      }
    })
  }

  clickUpdate(){
    let roleIds: any[] = [];
    for(let i=0; i< this.selectedRoles.length; i++){
      roleIds.push({
        id: this.selectedRoles[i].id,
        required: false
      })
    }
    this.policyPost.description = this.policyDetailForm.get('description')?.value;
    this.policyPost.config.roles = JSON.stringify(roleIds);
    this.policyPost.logic = this.selectedLogic;

    this.roleService.updateClientPolicy(this.policyPost).subscribe((rs) => {
      if(rs.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'Thành công',
            detail: 'Cập nhật thành công',
            life: 2000
          });
      }
      console.log(rs.data)
      this.reset();
    }, (err) => {
      this.reset()
    })
  }

  reset(){
    this.selectedRoles = [];
    this.selectedAssignRole = []
    this.ngOnInit()
  }

  clickRemoveRole(name: string){
    this.selectedRoles = this.selectedRoles.filter((role)=> {
      return role.name != name;
    });
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }

  clickAssignRole(){
    this.selectedAssignRole.forEach((e) => {
      if(!this.isContainName(e.name)){
        this.selectedRoles.push(e);
      }
    })
    this.visible = false;
    this.selectedAssignRole = [];
  }

  isContainName(name: string | undefined){
    for(let i = 0; i < this.selectedRoles.length; i ++ ){
      if(this.selectedRoles[i].name == name){
        return true;
      }
    }
    return false;
  }



}
