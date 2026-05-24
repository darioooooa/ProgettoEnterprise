import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MieiItinerari } from './miei-itinerari';

describe('MieiItinerari', () => {
  let component: MieiItinerari;
  let fixture: ComponentFixture<MieiItinerari>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MieiItinerari],
    }).compileComponents();

    fixture = TestBed.createComponent(MieiItinerari);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
