import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchermataUtente } from './schermata-utente';

describe('SchermataUtente', () => {
  let component: SchermataUtente;
  let fixture: ComponentFixture<SchermataUtente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchermataUtente],
    }).compileComponents();

    fixture = TestBed.createComponent(SchermataUtente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
