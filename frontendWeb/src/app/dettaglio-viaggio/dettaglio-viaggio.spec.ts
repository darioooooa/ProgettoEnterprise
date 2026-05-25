import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DettaglioViaggio } from './dettaglio-viaggio';

describe('DettaglioViaggio', () => {
  let component: DettaglioViaggio;
  let fixture: ComponentFixture<DettaglioViaggio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DettaglioViaggio],
    }).compileComponents();

    fixture = TestBed.createComponent(DettaglioViaggio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
