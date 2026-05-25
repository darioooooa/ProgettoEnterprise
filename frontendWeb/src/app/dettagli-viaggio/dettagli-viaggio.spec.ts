import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DettagliViaggio } from './dettagli-viaggio';

describe('DettagliViaggio', () => {
  let component: DettagliViaggio;
  let fixture: ComponentFixture<DettagliViaggio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DettagliViaggio],
    }).compileComponents();

    fixture = TestBed.createComponent(DettagliViaggio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
