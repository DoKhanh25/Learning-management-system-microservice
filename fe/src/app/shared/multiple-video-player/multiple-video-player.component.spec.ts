import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultipleVideoPlayerComponent } from './multiple-video-player.component';

describe('MultipleVideoPlayerComponent', () => {
  let component: MultipleVideoPlayerComponent;
  let fixture: ComponentFixture<MultipleVideoPlayerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MultipleVideoPlayerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MultipleVideoPlayerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
