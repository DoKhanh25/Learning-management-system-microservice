import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionBankDetailComponent } from './question-bank-detail.component';

describe('QuestionBankDetailComponent', () => {
  let component: QuestionBankDetailComponent;
  let fixture: ComponentFixture<QuestionBankDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [QuestionBankDetailComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuestionBankDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
