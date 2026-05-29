import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiventaOrganizzatore } from './diventa-organizzatore';

describe('DiventaOrganizzatore', () => {
  let component: DiventaOrganizzatore;
  let fixture: ComponentFixture<DiventaOrganizzatore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DiventaOrganizzatore],
    }).compileComponents();

    fixture = TestBed.createComponent(DiventaOrganizzatore);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
