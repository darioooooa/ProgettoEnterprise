import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModaleSegnalazione } from './modale-segnalazione';

describe('ModaleSegnalazione', () => {
  let component: ModaleSegnalazione;
  let fixture: ComponentFixture<ModaleSegnalazione>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModaleSegnalazione],
    }).compileComponents();

    fixture = TestBed.createComponent(ModaleSegnalazione);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
