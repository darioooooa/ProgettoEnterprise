import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SezionePagamento } from './sezione-pagamento';

describe('SezionePagamento', () => {
  let component: SezionePagamento;
  let fixture: ComponentFixture<SezionePagamento>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SezionePagamento],
    }).compileComponents();

    fixture = TestBed.createComponent(SezionePagamento);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
