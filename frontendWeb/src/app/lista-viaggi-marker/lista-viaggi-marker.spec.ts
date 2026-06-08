import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListaViaggiMarker } from './lista-viaggi-marker';

describe('ListaViaggiMarker', () => {
  let component: ListaViaggiMarker;
  let fixture: ComponentFixture<ListaViaggiMarker>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaViaggiMarker],
    }).compileComponents();

    fixture = TestBed.createComponent(ListaViaggiMarker);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
