import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Programma } from './programma';

describe('Programma', () => {
  let component: Programma;
  let fixture: ComponentFixture<Programma>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Programma],
    }).compileComponents();

    fixture = TestBed.createComponent(Programma);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
