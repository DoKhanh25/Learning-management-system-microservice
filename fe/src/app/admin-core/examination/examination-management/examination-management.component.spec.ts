import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExaminationManagementComponent } from './examination-management.component';

describe('ExaminationManagementComponent', () => {
  let component: ExaminationManagementComponent;
  let fixture: ComponentFixture<ExaminationManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExaminationManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExaminationManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
