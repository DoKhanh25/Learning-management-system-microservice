import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExaminationListComponent } from './examination-list.component';

describe('ExaminationListComponent', () => {
  let component: ExaminationListComponent;
  let fixture: ComponentFixture<ExaminationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExaminationListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExaminationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
