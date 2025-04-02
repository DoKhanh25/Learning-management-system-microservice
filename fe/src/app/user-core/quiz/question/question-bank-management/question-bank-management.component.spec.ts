import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionBankManagementComponent } from './question-bank-management.component';

describe('QuestionBankManagementComponent', () => {
  let component: QuestionBankManagementComponent;
  let fixture: ComponentFixture<QuestionBankManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [QuestionBankManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuestionBankManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
