import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticheOrganizzatore } from './statistiche-organizzatore';

describe('StatisticheOrganizzatore', () => {
  let component: StatisticheOrganizzatore;
  let fixture: ComponentFixture<StatisticheOrganizzatore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatisticheOrganizzatore],
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticheOrganizzatore);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
