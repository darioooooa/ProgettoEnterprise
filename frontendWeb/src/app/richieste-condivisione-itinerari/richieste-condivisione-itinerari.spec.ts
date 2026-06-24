import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RichiesteCondivisioneItinerari } from './richieste-condivisione-itinerari';

describe('RichiesteCondivisioneItinerari', () => {
  let component: RichiesteCondivisioneItinerari;
  let fixture: ComponentFixture<RichiesteCondivisioneItinerari>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RichiesteCondivisioneItinerari],
    }).compileComponents();

    fixture = TestBed.createComponent(RichiesteCondivisioneItinerari);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
