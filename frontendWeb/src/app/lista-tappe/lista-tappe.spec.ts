import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListaTappe } from './lista-tappe';

describe('ListaTappe', () => {
  let component: ListaTappe;
  let fixture: ComponentFixture<ListaTappe>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaTappe],
    }).compileComponents();

    fixture = TestBed.createComponent(ListaTappe);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
