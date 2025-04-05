import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExamEssayComponent } from './exam-essay.component';

describe('ExamEssayComponent', () => {
  let component: ExamEssayComponent;
  let fixture: ComponentFixture<ExamEssayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExamEssayComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExamEssayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
