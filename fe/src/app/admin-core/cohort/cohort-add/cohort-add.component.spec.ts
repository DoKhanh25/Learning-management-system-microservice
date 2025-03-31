import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CohortAddComponent } from './cohort-add.component';

describe('CohortAddComponent', () => {
  let component: CohortAddComponent;
  let fixture: ComponentFixture<CohortAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CohortAddComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CohortAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
