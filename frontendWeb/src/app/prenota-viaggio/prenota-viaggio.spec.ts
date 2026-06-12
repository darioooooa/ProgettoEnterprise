import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrenotaViaggio } from './prenota-viaggio';

describe('PrenotaViaggio', () => {
  let component: PrenotaViaggio;
  let fixture: ComponentFixture<PrenotaViaggio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrenotaViaggio],
    }).compileComponents();

    fixture = TestBed.createComponent(PrenotaViaggio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
