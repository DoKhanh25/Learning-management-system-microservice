import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CountryService} from "../../../services/country/country.service";
import {UserService} from "../../../services/user-service/user.service";
import {AddUser, Attribute} from "../../../../model/user";

@Component({
  selector: 'app-account-add',
  templateUrl: './account-add.component.html',
  styleUrl: './account-add.component.css'
})
export class AccountAddComponent implements OnInit{
  items: MenuItem[] | undefined;
  isCollapsed = true;
  isCollapsed2 = true;
  countries: any[] = [];
  cities: any[] = [];
  cityLoad = false;
  file!: File

  addUser!: AddUser

  sampleExcelUrl = "http://localhost:8082/api/getUsersExcelSample"



  addUserFormGroup!: FormGroup

  constructor(private fb: FormBuilder,
              private countryService: CountryService,
              private messageService: MessageService,
              private userService: UserService
              ) {
  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản trị tài khoản', url: '/admin/account-management' },
        {label: 'Tạo tài khoản'}
      ];
    this.addUserFormGroup = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      enable: [true, Validators.required],
      attributes: this.fb.group({
        phone: [''],
        address: [''],
        country: [''],
        city: [''],
        department: [''],
        institution: ['']
      })
    })


      this.countryService.getCountries().subscribe((data) => {
          this.countries = data.sort((a, b) => a.name.common.localeCompare(b.name.common));
      });
  }

    onCountryChange() {
        if (this.addUserFormGroup.get('attributes.country')?.value) {
            this.countryService.getCities(this.addUserFormGroup.get('attributes.country')?.value)
                .subscribe((res) => {
                this.cities = res.data;
                this.cityLoad = false;
            });
        } else {
            this.cities = [];
            this.cityLoad = false;
        }
    }

    onUpload(event:any) {
      this.messageService.add(
        {severity: 'info',
          summary: 'File Uploaded',
          detail: 'Tệp được đăng tải',
          life: 2000
        });
    }

    clickAddUser(){
        let attributes: Attribute = {
            phone: [this.addUserFormGroup.get('attributes.phone')?.value],
            address: [this.addUserFormGroup.get('attributes.address')?.value],
            city: [this.addUserFormGroup.get('attributes.city')?.value],
            country: [this.addUserFormGroup.get('attributes.country')?.value],
            department: [this.addUserFormGroup.get('attributes.department')?.value],
            institution: [this.addUserFormGroup.get('attributes.address')?.value]
        }
        this.addUser = {
            username: this.addUserFormGroup.get('username')?.value,
            lastName: this.addUserFormGroup.get('lastName')?.value,
            firstName: this.addUserFormGroup.get('firstName')?.value,
            enable: this.addUserFormGroup.get('enable')?.value,
            email: this.addUserFormGroup.get('email')?.value,
            password: this.addUserFormGroup.get('password')?.value,
            attributes: attributes,
            roles: [],
            groups: []
        }

        this.userService.addUser(this.addUser).subscribe((result) => {
            if(result.status == 409){
                return this.messageService.add(
                    {severity: 'warn',
                        summary: 'Không thành công',
                        detail: 'Tài khoản đã tồn tại',
                        life: 2000
                    });
            } else if(result.status == 0){
                return this.messageService.add(
                    {severity: 'warn',
                        summary: 'Không thành công',
                        detail: 'Tạo tài khoản không thành công',
                        life: 2000
                    });
            }
            this.messageService.add(
                {severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo tài khoản thành công',
                    life: 2000
                });
        }, (err) =>{
            this.messageService.add(
                {severity: 'error',
                    summary: 'Tạo tk không thành công',
                    detail: err,
                    life: 2000});
        })



    }

    uploadHandler(event: any){
      this.file = event.files[0];
      const formData = new FormData();
      formData.append("file", this.file);
      this.userService.addUserByExcel(formData).subscribe((response) => {
        this.downloadFile(response, "Ket_qua_tao_tai_khoan.xlsx");
        this.messageService.add(
          {severity: 'info',
            summary: 'File Uploaded',
            detail: 'Tệp được đăng tải',
            life: 2000
          });
      }, (err) => {
        this.messageService.add(
          {severity: 'error',
            summary: 'File Uploaded',
            detail: err,
            life: 2000});
      })
    }


  downloadFile(blob: Blob, fileName: string) {
    const link = document.createElement('a');
    const url = window.URL.createObjectURL(blob); // Tạo URL từ blob
    link.href = url;
    link.download = fileName; // Tên file tải xuống
    document.body.appendChild(link);
    link.click(); // Kích hoạt tải xuống
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url); // Xóa URL tạm
  }


}
