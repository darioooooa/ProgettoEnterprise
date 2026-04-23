import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreaViaggio } from './crea-viaggio';

describe('CreaViaggio', () => {
  let component: CreaViaggio;
  let fixture: ComponentFixture<CreaViaggio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreaViaggio],
    }).compileComponents();

    fixture = TestBed.createComponent(CreaViaggio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
