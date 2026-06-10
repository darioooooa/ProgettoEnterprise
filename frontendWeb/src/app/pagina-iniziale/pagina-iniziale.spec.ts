import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaginaIniziale } from './pagina-iniziale';

describe('PaginaIniziale', () => {
  let component: PaginaIniziale;
  let fixture: ComponentFixture<PaginaIniziale>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginaIniziale],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginaIniziale);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
