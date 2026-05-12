import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchermataPrenotazioni } from './schermata-prenotazioni';

describe('SchermataPrenotazioni', () => {
  let component: SchermataPrenotazioni;
  let fixture: ComponentFixture<SchermataPrenotazioni>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchermataPrenotazioni],
    }).compileComponents();

    fixture = TestBed.createComponent(SchermataPrenotazioni);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
